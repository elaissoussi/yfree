
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;

import de.hybris.platform.util.Utilities;


byte[] response = [];

File logDir = Utilities.getPlatformConfig().getSystemConfig().getLogDir();

OutputStream out = new FileOutputStream(logDir. getPath()+"/out.pdf");

out.write(response);

out.close();
