package net.ttddyy.dsproxy.listener.logging;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;

import java.util.function.BooleanSupplier;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.3
 */
public abstract class AbstractQueryLoggingListener implements ProxyDataSourceListener {

    protected QueryLogEntryCreator queryLogEntryCreator = new DefaultQueryLogEntryCreator();
    protected boolean writeConnectionId = true;
    protected BooleanSupplier loggingCondition;

    @Override
    public void afterQuery(ExecutionInfo execInfo) {
        // only perform logging logic when the condition returns true
        if (this.loggingCondition.getAsBoolean()) {
            String entry = getEntry(execInfo);
            writeLog(entry);
        }
    }

    protected String getEntry(ExecutionInfo execInfo) {
        boolean writeDataSourceName = execInfo.getDataSourceName() != null && !execInfo.getDataSourceName().trim().equals("");
        return this.queryLogEntryCreator.getLogEntry(execInfo, writeDataSourceName, this.writeConnectionId);
    }

    protected abstract void writeLog(String message);

    public void setQueryLogEntryCreator(QueryLogEntryCreator queryLogEntryCreator) {
        this.queryLogEntryCreator = queryLogEntryCreator;
    }

    /**
     * @return query log entry creator
     * @since 1.4.1
     */
    public QueryLogEntryCreator getQueryLogEntryCreator() {
        return queryLogEntryCreator;
    }

    /**
     * @since 1.4.2
     */
    public void setWriteConnectionId(boolean writeConnectionId) {
        this.writeConnectionId = writeConnectionId;
    }

    /**
     * A boolean supplier that determines whether to perform logging logic.
     *
     * @param loggingCondition boolean supplier
     * @since 1.4.3
     */
    public void setLoggingCondition(BooleanSupplier loggingCondition) {
        this.loggingCondition = loggingCondition;
    }
}
