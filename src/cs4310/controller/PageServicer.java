package cs4310.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cs4310.Main;
/*
 * This class instantiates the HTTP Server and handles the page request
 */
public class PageServicer {
	HttpServer server;
	InetSocketAddress address;
	PageHandler ph = new PageHandler();
	public PageServicer() {
		try {
			this.address = new InetSocketAddress(8080);
			server = HttpServer.create(this.address, 0);
			server.createContext("/", ph);
			ph.newServer= this.server;
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public PageServicer(InetSocketAddress address) {
		try {
			this.address = address;
			server = HttpServer.create(address, 0);
			server.createContext("/", ph);
			ph.newServer= this.server;
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	static class PageHandler implements HttpHandler {
		HttpServer newServer;
                @Override
		public void handle(HttpExchange t) throws IOException {
			System.out.println("Request Method:\t" + t.getRequestMethod());
			System.out.println("Request Body:\t" + t.getRequestBody());
			System.out.println("Request Headers:\t" + t.getRequestHeaders());
			System.out.println("Request URI:\t" + t.getRequestURI());
			System.out.println("Request URI path:\t" + t.getRequestURI());
			String pageFileName = t.getRequestURI().toString();
			pageFileName = pageFileName.replace("/", "");
			File file = getPageFile(pageFileName);
			String str = new String();
			if (file == null) {
				String response = "404 Error: Page Not Found";
				t.sendResponseHeaders(404, response.length());
				OutputStream outStream = t.getResponseBody();
				outStream.write(response.getBytes());
				outStream.flush();
				outStream.close();
			} else {
				OutputStream outStream = t.getResponseBody();
				FileInputStream fileStream = new FileInputStream(file);
				final byte[] buffer = new byte[0x10000];
				int count = 0;
				while ((count = fileStream.read()) >= 0) {
					str = str+(char)count;
				}
				str = str.replace("%s", this.newServer.getAddress().getHostString());
				t.sendResponseHeaders(200, 0);
				//System.out.print(str);
				outStream.write(str.getBytes());
				outStream.flush();
				outStream.close();
				fileStream.close();
			}
		}
		private File getPageFile(String f) {
			File respFile = null;
			if (f.equals("style.css")) {
                            if ( Main.isUsingSrcFolderAsCWD() )
				respFile = new File("cs4310/view/style.css");
                            else
                                respFile = new File("src/cs4310/view/style.css");
			} else if (f.equals("editprofile.html")) {
                            if ( Main.isUsingSrcFolderAsCWD() )
				respFile = new File("cs4310/view/editprofile.html");
                            else
                                respFile = new File("src/cs4310/view/editprofile.html");
			} else if (f.equals("chatsite.html")){
                            if ( Main.isUsingSrcFolderAsCWD() )
				respFile = new File("cs4310/view/chatsite.html");
                            else
                                respFile = new File("src/cs4310/view/chatsite.html");
			} else if (f.equals("register.html")){
                            if ( Main.isUsingSrcFolderAsCWD() )
				respFile = new File("cs4310/view/register.html");
                            else
                                respFile = new File("src/cs4310/view/register.html");
			}else 
			{
                            if ( Main.isUsingSrcFolderAsCWD() )
                                respFile = new File("cs4310/view/login.html");
                            else
                                respFile = new File("src/cs4310/view/login.html");
			}
                        
			return respFile;
		}
	}
}
