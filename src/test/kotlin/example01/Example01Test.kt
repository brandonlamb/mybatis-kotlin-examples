package example01

import org.assertj.core.api.Assertions.*
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.sql.Date
import java.sql.DriverManager
import java.time.LocalDate

class Example01Test {
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeEach
    fun setup() {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/CreateSimpleDB.sql")
        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.addMapper(Example01Mapper::class.java)
        sqlSessionFactory = SqlSessionFactoryBuilder().build(config)
    }

    @Test
    fun selectPersonWithAllFields() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(Example01Mapper::class.java)

            val person = mapper.selectPersonById(1)

            assertThat(person.id).isEqualTo(1)
            assertThat(person.firstName).isEqualTo("Fred")
            assertThat(person.lastName).isEqualTo("Flintstone")
            assertThat(person.birthDate).isEqualTo(Date.valueOf(LocalDate.of(1935, 2, 1)))
            assertThat(person.employed).isEqualToIgnoringCase("Yes")
            assertThat(person.occupation).isEqualTo("Brontosaurus Operator")
        }
    }

    @Test
    fun selectPersonWithNullOccupation() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(Example01Mapper::class.java)

            val person = mapper.selectPersonById(3)

            assertThat(person.id).isEqualTo(3)
            assertThat(person.firstName).isEqualTo("Pebbles")
            assertThat(person.lastName).isEqualTo("Flintstone")
            assertThat(person.birthDate).isEqualTo(Date.valueOf(LocalDate.of(1960, 5, 6)))
            assertThat(person.employed).isEqualToIgnoringCase("No")
            assertThat(person.occupation).isNull()
        }
    }

    @Test
    fun selectPersonWithNullOccupationAndElvisOperator() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(Example01Mapper::class.java)

            val person = mapper.selectPersonById(3)

            assertThat(person.id).isEqualTo(3)
            assertThat(person.firstName).isEqualTo("Pebbles")
            assertThat(person.lastName).isEqualTo("Flintstone")
            assertThat(person.birthDate).isEqualTo(Date.valueOf(LocalDate.of(1960, 5, 6)))
            assertThat(person.employed).isEqualToIgnoringCase("No")
            assertThat(person.occupation ?: "<null>").isEqualTo("<null>")
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
