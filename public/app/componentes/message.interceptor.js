/**
 * Created by clairton on 06/01/16.
 */
(function () {
    'use strict';

    angular.module('sicobaApp')
        .factory('MessageInterceptor', function ($rootScope, $q) {
            return {
                request: function (config) {
                    $rootScope.messages = [];
                    return config;
                },
                responseError: function (rejection) {
                    if (rejection.status !== 401) {
                        if (rejection.data && rejection.data.errors) {
                            rejection.data.errors.forEach(function (it) {
                                $rootScope.messages.push({
                                    title: 'Error:' + it.field,
                                    body: it.defaultMessage,
                                    type: 'alert-danger'
                                });
                            });
                        } else {
                            var message = rejection.message;
                            if (rejection.data && rejection.data.message) {
                                message = rejection.data.message;
                            }
                            $rootScope.messages.push({
                                title: 'Error:',
                                body: message,
                                type: 'alert-danger'
                            });
                        }
                    }

                    return $q.reject(rejection);
                }
            };
        })
        .config(function ($httpProvider) {
            $httpProvider.interceptors.push('MessageInterceptor');
        });
}());
