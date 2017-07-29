package jp.gr.naoco.sample.dummy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class DummyDatabaseMetadata implements DatabaseMetaData {

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public String getURL() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getUserName() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getDriverName() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return "Oracle JDBC driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public int getDriverMajorVersion() {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getDriverMinorVersion() {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getStringFunctions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
            String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
            String attributeNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
            String columnNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
            String columnNamePattern) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

}
