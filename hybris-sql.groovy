import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import de.hybris.platform.util.Utilities;
import de.hybris.platform.core.Registry;

Connection conn = null;
PreparedStatement pstmt = null;

try
{

    conn = Registry.getCurrentTenant().getDataSource().getConnection();

    pstmt = conn.prepareStatement("your sql query here...");

    pstmt.execute();

}
catch (final SQLException e)
{
    LOG.error("Error!!");
}
finally
{
    Utilities.tryToCloseJDBC(conn, pstmt, null);
}

return "Groovy Rocks!"

