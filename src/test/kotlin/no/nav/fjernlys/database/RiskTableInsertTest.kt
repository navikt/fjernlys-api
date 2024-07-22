//package no.nav.fjernlys.dbQueries
//
//import kotliquery.queryOf
//import kotliquery.sessionOf
//import kotliquery.using
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import testcontainers.TestContainersHelper
//
//class DbQueryInsertTest {
//
//    private lateinit var dbQueryInsert: DbQueryInsert
//    private val dataSource = TestContainersHelper.postgresContainer.dataSource
//
//    @BeforeEach
//    fun setup() {
//        dbQueryInsert = DbQueryInsert(dataSource)
//        clearDatabase()
//    }
//
//    @AfterEach
//    fun teardown() {
//        clearDatabase()
//    }
//
//    @Test
//    fun testInsertInfo() {
//        dbQueryInsert.insertInfo()
//
//        using(sessionOf(dataSource)) { session ->
//            val sql = "SELECT * FROM risk_report"
//            val query = queryOf(sql).map { row -> dbQueryInsert.mapRowToRisikoRapport(row) }.asList
//            val results = session.run(query)
//
//
//            // Additional assertions can be added here as needed
//        }
//    }
//
//
//    private fun clearDatabase() {
//        using(sessionOf(dataSource)) { session ->
//            val sql = "DELETE FROM risk_report"
//            val query = queryOf(sql).asUpdate
//            session.run(query)
//        }
//    }
//}
