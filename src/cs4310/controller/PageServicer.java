<<<<<<< HEAD
package cs4310.controller;

=======
>>>>>>> 4f45ecedfe82c05a0e1774fe1d921a9847034be7
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
/*
 * This class instantiates the HTTP Server and handles the page request
 */
public class PageServicer {
	public PageServicer() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/", new PageHandler());
			server.setExecutor(null); 
			server.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

    public PageServicer(InetSocketAddress address) {
		try {
			HttpServer server = HttpServer.create(address, 0);
			server.createContext("/", new PageHandler());
			server.setExecutor(null); 
			server.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	static class PageHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			System.out.println("Request Method:\t" + t.getRequestMethod());
			System.out.println("Request Body:\t" + t.getRequestBody());
			System.out.println("Request Headers:\t" + t.getRequestHeaders());
			System.out.println("Request URI:\t" + t.getRequestURI());
			System.out.println("Request URI path:\t" + t.getRequestURI());
			String pageFileName = t.getRequestURI().toString();
			pageFileName = pageFileName.replace("/", "");
			File file = getPageFile(pageFileName);
			if (file == null) {
				String response = "404 Error: Page Not Found";
				t.sendResponseHeaders(404, response.length());
				OutputStream outStream = t.getResponseBody();
				outStream.write(response.getBytes());
				outStream.flush();
				outStream.close();
			} else {
				t.sendResponseHeaders(200, 0);
				OutputStream outStream = t.getResponseBody();
				FileInputStream fileStream = new FileInputStream(file);
				final byte[] buffer = new byte[0x10000];
				int count = 0;
				while ((count = fileStream.read(buffer)) >= 0) {
					outStream.write(buffer, 0, count);
				}
				outStream.flush();
				outStream.close();
				fileStream.close();
			}
		}
		private File getPageFile(String f) {
			File test = null;
			if (f.equals("style.css")) {
				test = new File("./src/cs4310/view/style.css");
			} else if (f.equals("editprofile.html")) {
				test = new File("./src/cs4310/view/editprofile.html");
			} else{
				test = new File("./src/cs4310/view/chatsite.html");
			}
			return test;
		}
	}
}
