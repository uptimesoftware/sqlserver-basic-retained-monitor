package com.uptimesoftware.uptime.erdc;

import java.sql.SQLException;

import com.uptimesoftware.uptime.erdc.baseclass.SqlServerQueryingMonitor;
import com.uptimesoftware.uptime.erdc.database.RemoteConnection;
import com.uptimesoftware.uptime.erdc.database.SearchableQuery;

public class MonitorSQLRetained extends SqlServerQueryingMonitor {

    protected Boolean hasErrors = false;

    public MonitorSQLRetained() {
        super();
        setSimpleMonitor();
    }

    @Override
    protected void doWork(RemoteConnection connection) throws SQLException {
        if (sqlScript.isEmpty()) {
            setMessage("Connected to SQL server on port " + getPort());
        } else {
            
            boolean matches = queryHasMatch(connection, sqlScript, matchTerm);

            if (matches) {
                setResultStatus(ErdcTransientState.OK, "'" + matchTerm + "'" + " was found in SQL result.");
            } else {
                setResultStatus(ErdcTransientState.CRIT, "No matches in SQL result");
            }
        }
    }

    private boolean queryHasMatch(RemoteConnection connection, String sqlScript, String needle) {
        String formattedSql = sqlScript.replace('\"', '\'');
        SearchableQuery searchableQuery = new SearchableQuery(connection, formattedSql, needle);
        Boolean result = searchableQuery.loadRemotely();

        int errors = 0;
        String row_str = searchableQuery.getRow();
        addVariable("textoutput", row_str);
        //long row_long = searchableQuery.getRowAsLong();
        try {
            long row_long = Integer.parseInt(row_str.trim());
            addVariable("numberoutput", row_long);
        } catch (Exception ex) {
            errors++;
            setState(ErdcTransientState.CRIT);
            setMessage("Error: " + ex.getMessage() + "\rQuery output: " + row_str);
        }

        if (errors == 0) {
            setMessage("Query completed successfully.");
        }
        else {
            hasErrors = true;
            setMessage("Query completed with errors.");
        }


        return result;
    }
}
