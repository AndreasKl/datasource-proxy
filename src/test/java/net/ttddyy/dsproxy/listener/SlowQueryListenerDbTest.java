package net.ttddyy.dsproxy.listener;

import net.ttddyy.dsproxy.DatabaseTest;
import net.ttddyy.dsproxy.DatabaseType;
import net.ttddyy.dsproxy.DbResourceCleaner;
import net.ttddyy.dsproxy.EnabledOnDatabase;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JSlowQueryListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Tadaya Tsuyukubo
 */
@DatabaseTest
public class SlowQueryListenerDbTest {

    private DataSource jdbcDataSource;
    private DbResourceCleaner cleaner;

    public SlowQueryListenerDbTest(DataSource jdbcDataSource, DbResourceCleaner cleaner) {
        this.jdbcDataSource = jdbcDataSource;
        this.cleaner = cleaner;
    }

    @Test
    public void onSlowQuery() throws Exception {

        final ExecutionInfo executionInfo = new ExecutionInfo();

        final AtomicInteger counter = new AtomicInteger();

        SlowQueryListener listener = new SlowQueryListener() {
            @Override
            protected void onSlowQuery(ExecutionInfo execInfo, long startTimeInMills) {
                counter.incrementAndGet();
                assertThat(execInfo).isSameAs(executionInfo);
            }
        };
        listener.setThreshold(50);
        listener.setThresholdTimeUnit(TimeUnit.MILLISECONDS);

        // simulate slow query
        listener.beforeQuery(executionInfo);
        TimeUnit.MILLISECONDS.sleep(200);  // ample time
        listener.afterQuery(executionInfo);

        assertThat(counter.get()).as("callback should be called, and it should be once").isEqualTo(1);
    }

    @Test
    public void onSlowQueryWithFastQuery() throws Exception {

        SlowQueryListener listener = new SlowQueryListener() {
            @Override
            protected void onSlowQuery(ExecutionInfo execInfo, long startTimeInMills) {
                fail("onSlowQuery method should not be called for fast query");
            }
        };
        listener.setThreshold(100);
        listener.setThresholdTimeUnit(TimeUnit.MILLISECONDS);

        final ExecutionInfo executionInfo = new ExecutionInfo();

        // calling immediately after
        listener.beforeQuery(executionInfo);
        listener.afterQuery(executionInfo);
    }


    @EnabledOnDatabase(DatabaseType.HSQL)
    public void executionTime() throws Exception {

        final AtomicLong executionTime = new AtomicLong(0);
        QueryLogEntryCreator queryLogEntryCreator = new DefaultQueryLogEntryCreator() {
            @Override
            public String getLogEntry(ExecutionInfo execInfo, boolean writeDataSourceName, boolean writeConnectionId) {
                executionTime.set(execInfo.getElapsedTime());
                return super.getLogEntry(execInfo, writeDataSourceName, writeConnectionId);
            }
        };

        SLF4JSlowQueryListener listener = new SLF4JSlowQueryListener(100, TimeUnit.MILLISECONDS);
        listener.setQueryLogEntryCreator(queryLogEntryCreator);


        ProxyDataSource pds = ProxyDataSourceBuilder.create(jdbcDataSource).listener(listener).build();

        String funcSleep = "CREATE FUNCTION funcSleep()" +
                " RETURNS INTEGER" +
                " LANGUAGE JAVA DETERMINISTIC NO SQL" +
                " EXTERNAL NAME 'CLASSPATH:net.ttddyy.dsproxy.listener.SlowQueryListenerDbTest.funcSleep'";

        Connection conn = pds.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.execute(funcSleep);

        CallableStatement cs = conn.prepareCall("CALL funcSleep()");
        this.cleaner.add(cs);
        cs.execute();

        assertThat(executionTime.get()).as("execInfo.elapsedTime should be populated").isGreaterThanOrEqualTo(100).isLessThan(300);
    }


    /**
     * hsqldb function to sleep 200 msec
     */
    public static int funcSleep() throws Exception {
        TimeUnit.MILLISECONDS.sleep(200);
        return 0;
    }


}
