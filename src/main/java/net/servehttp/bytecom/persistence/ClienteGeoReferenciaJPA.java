package net.servehttp.bytecom.persistence;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;

import net.servehttp.bytecom.persistence.entity.cadastro.QCliente;
import net.servehttp.bytecom.persistence.entity.maps.ClienteGeoReferencia;
import net.servehttp.bytecom.persistence.entity.maps.QClienteGeoReferencia;

/**
 * 
 * @author Felipe W. M. Martins
 *
 */
@Transactional
public class ClienteGeoReferenciaJPA implements Serializable {

  private static final long serialVersionUID = 7468802847947425443L;
  @PersistenceContext(unitName = "bytecom-pu")
  private EntityManager em;
  private QClienteGeoReferencia cg = QClienteGeoReferencia.clienteGeoReferencia;
  private QCliente c = QCliente.cliente;
  
  public void setEntityManager(EntityManager em) {
    this.em = em;
  }
  
  public List<ClienteGeoReferencia> buscaClientesGeo(){
    return new JPAQuery(em)
              .from(cg).list(cg);
  }
  
  
  

}
