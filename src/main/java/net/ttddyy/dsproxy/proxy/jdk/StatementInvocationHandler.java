package net.ttddyy.dsproxy.proxy.jdk;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.proxy.InterceptorHolder;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.StatementProxyLogic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Proxy InvocationHandler for {@link java.sql.Statement}.
 *
 * @author Tadaya Tsuyukubo
 */
public class StatementInvocationHandler implements InvocationHandler {

    private StatementProxyLogic delegate;

    public StatementInvocationHandler(
            Statement stmt, InterceptorHolder interceptorHolder, ConnectionInfo connectionInfo,
            Connection proxyConnection, JdbcProxyFactory proxyFactory) {
        this.delegate = StatementProxyLogic.Builder.create()
                .statement(stmt)
                .interceptorHolder(interceptorHolder)
                .connectionInfo(connectionInfo)
                .proxyConnection(proxyConnection)
                .proxyFactory(proxyFactory)
                .build();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return delegate.invoke(method, args);
    }
}
