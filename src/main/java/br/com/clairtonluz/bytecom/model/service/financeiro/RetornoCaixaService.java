package br.com.clairtonluz.bytecom.model.service.financeiro;

import br.com.clairtonluz.bytecom.commons.parse.ParseRetornoCaixa;
import br.com.clairtonluz.bytecom.model.jpa.entity.comercial.Cliente;
import br.com.clairtonluz.bytecom.model.jpa.entity.comercial.Conexao;
import br.com.clairtonluz.bytecom.model.jpa.entity.comercial.Contrato;
import br.com.clairtonluz.bytecom.model.jpa.entity.comercial.StatusCliente;
import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.Titulo;
import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.StatusTitulo;
import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.retorno.Header;
import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.retorno.HeaderLote;
import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.retorno.Registro;
import br.com.clairtonluz.bytecom.model.jpa.financeiro.HeaderJPA;
import br.com.clairtonluz.bytecom.model.repository.comercial.ClienteRepository;
import br.com.clairtonluz.bytecom.model.repository.comercial.ContratoRepository;
import br.com.clairtonluz.bytecom.model.repository.financeiro.TituloRepository;
import br.com.clairtonluz.bytecom.model.service.comercial.conexao.ConexaoService;
import br.com.clairtonluz.bytecom.model.service.provedor.IConnectionControl;
import br.com.clairtonluz.bytecom.pojo.financeiro.RetornoPojo;
import br.com.clairtonluz.bytecom.util.web.AlertaUtil;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by clairtonluz on 31/10/15.
 */
public class RetornoCaixaService implements Serializable {
    private static ParseRetornoCaixa PARSE_RETORNO = new ParseRetornoCaixa();

    @Inject
    private TituloRepository tituloRepository;
    @Inject
    private HeaderJPA headerJPA;
    @Inject
    private ClienteRepository clienteRepository;
    @Inject
    private ContratoRepository contratoRepository;
    @Inject
    private ConexaoService conexaoService;
    @Inject
    private IConnectionControl connectionControl;

    public Header parse(InputStream inputStream, String filename) throws IOException {
        return PARSE_RETORNO.parse(inputStream, filename);
    }

    @Transactional
    public List<RetornoPojo> processarHeader(Header header) throws Exception {
        List<RetornoPojo> retornoPojos = new ArrayList<>();
        if (notExists(header)) {
            List<Titulo> titulosRegistradas = new ArrayList<>();
            for (HeaderLote hl : header.getHeaderLotes()) {
                for (Registro r : hl.getRegistros()) {
                    if (r.getCodigoMovimento() == Registro.ENTRADA_CONFIRMADA) {
                        titulosRegistradas.add(criarTituloRegistrada(r));
                    } else if (r.getCodigoMovimento() == Registro.LIQUIDACAO) {
                        Titulo m = liquidarTitulo(r);
                        RetornoPojo pojo = new RetornoPojo();

                        pojo.setTitulo(m);
                        pojo.setMovimento("LIQUIDAÇÂO");
                        retornoPojos.add(pojo);
                    }
                }
            }
            titulosRegistradas.forEach((it) -> {
                tituloRepository.save(it);
            });
            titulosRegistradas.forEach(titulo -> {
                Titulo m = (Titulo) titulo;
                RetornoPojo r = new RetornoPojo();

                r.setTitulo(m);
                r.setMovimento("ENTRADA CONFIRMADA");
                retornoPojos.add(r);
            });
            headerJPA.save(header);
        }
        return retornoPojos;
    }

    @Transactional
    private Titulo liquidarTitulo(Registro r) throws Exception {
        Titulo m = tituloRepository.findOptionalByNumeroBoleto(r.getNossoNumero());

        if (m != null) {
            m.setStatus(StatusTitulo.PAGO_NO_BOLETO);
            m.setValor(r.getValorTitulo());
            m.setValorPago(r.getRegistroDetalhe().getValorPago());
            m.setDesconto(r.getRegistroDetalhe().getDesconto());
            m.setTarifa(r.getValorTarifa());
            m.setDataOcorrencia(r.getRegistroDetalhe().getDataOcorrencia());
            tituloRepository.save(m);

            if (m.getCliente().getStatus().equals(StatusCliente.INATIVO)) {
                m.getCliente().setStatus(StatusCliente.ATIVO);

                Cliente cliente = m.getCliente();
                Contrato contrato = contratoRepository.findOptionalByCliente_id(cliente.getId());
                Conexao conexao = conexaoService.buscarOptionalPorCliente(cliente);
                clienteRepository.save(m.getCliente());
                conexaoService.save(conexao);
            }
        }
        return m;
    }

    private Titulo criarTituloRegistrada(Registro r) {
        String[] split = r.getNumeroDocumento().split("-");
        int clienteId = Integer.parseInt(split[0]);
        Cliente c = clienteRepository.findBy(clienteId);
        Titulo m = new Titulo();
        m.setCliente(c);
        m.setDataVencimento(r.getVencimento());
        m.setDesconto(r.getRegistroDetalhe().getDesconto());
        m.setModalidade(r.getModalidadeNossoNumero());
        m.setNumeroBoleto(r.getNossoNumero());
        m.setValor(r.getValorTitulo());
        return m;
    }

    private boolean notExists(Header header) {
        boolean exists = false;
        List<Header> list = headerJPA.buscarTodosPorSequencial(header.getSequencial());
        if (!list.isEmpty()) {
            exists = true;
            AlertaUtil.error("Arquivo já foi enviado");
        }
        return !exists;
    }
}
