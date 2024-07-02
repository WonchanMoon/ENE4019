package Assignment1_code_MoonWonchan_2019008813;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.sun.net.httpserver.*;
import com.google.gson.*;
import java.net.HttpCookie;
import java.net.InetSocketAddress;

public class Main {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		System.out.println("Listening in port: 8080");
		server.createContext("/", new RootHandler());
		server.createContext("/chair", new ChairHandler());
		server.createContext("/table", new TableHandler());
		server.createContext("/closet", new ClosetHandler());
		server.createContext("/favicon.ico", new FaviconHandler());
		server.setExecutor(null);
		server.start();
	}
	
	// 파일 줄마다 읽는 함수
	private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
        	e.getMessage();
        }
        return content.toString();
    }
	
	// Cookie 체크
	private static HttpExchange cookieCheck(HttpExchange exchange) {
		// 클라이언트로부터의 HTTP 요청에서 쿠키 확인
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                // 쿠키 헤더에서 쿠키 값을 추출
                String[] cookies = cookieHeader.split(";");
                for (String cookie : cookies) {
                    String[] parts = cookie.trim().split("=");
                    String name = parts[0];
                    String value = parts[1];
                    System.out.println("Returning user, welcome " + value);
                }
            }
        } else {
        	// 쿠키 생성 및 설정
        	System.out.println("New user requested page, cookie will be set.");
            HttpCookie cookie = new HttpCookie("StudentNumber", "2019008813");
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 쿠키 유효 시간 (1시간)
            exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
        }
        return exchange;
	}
	
	// JSON 파싱 후 detail HTML에 적용
	private static String jsonToHtml(String htmlContent, int num) throws IOException {
		
		// JSON 읽기
		String jsonFilePath = "./resources/furniture.json";
        String jsonContent = readFile(jsonFilePath);
        JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("Furniture");
        JsonElement jsonElement = jsonArray.get(num);
        
        // 가구에 맞게 HTML 수정
        String Name = jsonElement.getAsJsonObject().get("Name").getAsString();
        htmlContent = htmlContent.replace("<h1>OBJECT TITLE</h1>", "<h1>" + Name + "</h1>");
        String Price = jsonElement.getAsJsonObject().get("Price").getAsString();
        htmlContent = htmlContent.replace("<h3>OBJECT PRICE</h3>", "<h3>" + Price + "</h3>");
        String Desc = jsonElement.getAsJsonObject().get("Description").getAsString();
        htmlContent = htmlContent.replace("<p>OBJECT DESCRIPTION</p>", "<p>" + Desc + "</p>");
        String imgPath = jsonElement.getAsJsonObject().get("ImageLocation").getAsString();
        
        // 이미지 파일을 바이트 배열로 읽기
        byte[] imgBytes = Files.readAllBytes(Paths.get("./resources/"+imgPath));
        // 바이트 배열을 Base64 문자열로 인코딩
        String base64Img = Base64.getEncoder().encodeToString(imgBytes);
        htmlContent = htmlContent.replace("\"\" ","data:image/png;base64,"+base64Img + " ");
		
		return htmlContent;
	}
	
	// ROOT
	static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	System.out.println("Index page requested");
        	OutputStream os = exchange.getResponseBody();
        	
        	//쿠키 체크
        	exchange = cookieCheck(exchange);
        	
            try {
            	// HTML 파일을 읽고 쓰기
            	String htmlFilePath = "./resources/index.html";
                String htmlContent = readFile(htmlFilePath);
                byte[] htmlBytes = htmlContent.getBytes();
                exchange.sendResponseHeaders(200, htmlBytes.length);
                os.write(htmlBytes);
            } catch (IOException e) {
            	System.out.println("Failed : ROOT");
                // 파일을 찾지 못하면 404
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                os.write(response.getBytes());
            }
            os.close();
        }
    }
	
	// Chair
	static class ChairHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	System.out.println("Chair page requested");
        	OutputStream os = exchange.getResponseBody();
        	
        	// 쿠키 체크
        	exchange = cookieCheck(exchange);

            try {
                // HTML 파일을 읽기
            	String htmlFilePath = "./resources/detail.html";
                String htmlContent = readFile(htmlFilePath);
                
                // JSON으로부터 HTML 수정
                htmlContent = jsonToHtml(htmlContent, 0);
                
                // 바이트 리스트로 변환 후 첨부
                byte[] htmlBytes = htmlContent.getBytes();
                exchange.sendResponseHeaders(200, htmlBytes.length);
                os.write(htmlBytes);
            } catch (IOException e) {
            	System.out.println("Failed : Chair");
                // 파일을 찾지 못하면 404
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                os.write(response.getBytes());
            }
            os.close();
        }
    }
	
	// Table
	static class TableHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	System.out.println("Table page requested");
        	OutputStream os = exchange.getResponseBody();
   
            // 쿠키 체크
        	exchange = cookieCheck(exchange);
            
            try {
            	// HTML 파일을 읽기
            	String htmlFilePath = "./resources/detail.html";
                String htmlContent = readFile(htmlFilePath);
                
                // JSON으로부터 HTML 수정
                htmlContent = jsonToHtml(htmlContent, 1);
                
                // 바이트 리스트로 변환 후 첨부
                byte[] htmlBytes = htmlContent.getBytes();
                exchange.sendResponseHeaders(200, htmlBytes.length);
                os.write(htmlBytes);
            } catch (IOException e) {
            	System.out.println("Failed : Table");
                // 파일을 찾지 못하면 404
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                os.write(response.getBytes());
            }
            os.close();
        }
    }
	
	// Closet
	static class ClosetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	System.out.println("Closet page requested");
        	OutputStream os = exchange.getResponseBody();

         	// 쿠키 체크
        	exchange = cookieCheck(exchange);
            
            try {
            	// HTML 파일을 읽기
            	String htmlFilePath = "./resources/detail.html";
                String htmlContent = readFile(htmlFilePath);
                
                // JSON으로부터 HTML 수정
                htmlContent = jsonToHtml(htmlContent, 2);
                
                // 바이트 리스트로 변환 후 첨부
                byte[] htmlBytes = htmlContent.getBytes();
                exchange.sendResponseHeaders(200, htmlBytes.length);
                os.write(htmlBytes);
            } catch (IOException e) {
                // 파일을 찾지 못하면 404
            	System.out.println("Failed : Closet");
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                os.write(response.getBytes());
            }
            os.close();
        }
    }
	
	// Favicon 예외처리
	static class FaviconHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
	       OutputStream os = exchange.getResponseBody();
	       String response = "";
           exchange.sendResponseHeaders(200, response.length());
           os.write(response.getBytes());
	       os.close();
	       }
	}
}
