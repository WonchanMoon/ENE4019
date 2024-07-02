package Assignment2_code_MoonWonchan_2019008813;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

import java.net.ServerSocket;
import java.net.Socket;

public class Main2 {

    public static void main(String[] args) {
        int port = 8080;
        try {
        	// 서버 소켓 생성
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Listening in port: " + port);

            while (true) {
            	// 클라이언트로부터 연결을 수락하고 클라이언트 소켓 받음
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 요청 핸들링 함수
    private static void handleRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            // 클라이언트로부터의 요청을 읽기
            String request = in.readLine();
            
            // 요청 메서드와 요청 URI 추출
            String[] requestParts = request.split(" ");
            String method = requestParts[0];
            String uri = requestParts[1];
            
            // 쿠키 체크
            String cookieValue = cookieCheck(in);
            
            // GET 요청에 대한 응답
            if ("GET".equals(method)) {
                handleGetRequest(uri, out, cookieValue);
            } else {
                // 지원하지 않는 HTTP 메서드에 대한 응답
                sendResponse(out, "HTTP/1.1 405 Method Not Allowed\r\n\r\nMethod Not Allowed", null, cookieValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 소켓 닫기
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // get 요청 핸들링 함수
    private static void handleGetRequest(String uri, OutputStream out, String cookieValue) throws IOException {

        // 각 경로마다 핸들링
        if ("/".equals(uri)) {
        	System.out.println("Index page requested");
        	
        	// HTML 파일을 읽고 쓰기
        	String htmlFilePath = "./resources/index.html";
            String htmlContent = readFile(htmlFilePath);
            byte[] htmlBytes = htmlContent.getBytes();
            
            sendResponse(out, "HTTP/1.1 200 OK\r\n\r\n", htmlBytes, cookieValue);
            
        } else if ("/chair".equals(uri)) {
        	System.out.println("Chair page requested");
        	
        	// HTML 파일을 읽기
        	String htmlFilePath = "./resources/detail.html";
            String htmlContent = readFile(htmlFilePath);
            
            // JSON으로부터 HTML 수정
            htmlContent = jsonToHtml(htmlContent, 0);
            
            // 바이트 리스트로 변환 후 첨부
            byte[] htmlBytes = htmlContent.getBytes();
            sendResponse(out, "HTTP/1.1 200 OK\r\n\r\n", htmlBytes, cookieValue);
        	
        } else if ("/table".equals(uri)) {
        	System.out.println("Table page requested");
        	
        	// HTML 파일을 읽기
        	String htmlFilePath = "./resources/detail.html";
            String htmlContent = readFile(htmlFilePath);
            
            // JSON으로부터 HTML 수정
            htmlContent = jsonToHtml(htmlContent, 1);
            
            // 바이트 리스트로 변환 후 첨부
            byte[] htmlBytes = htmlContent.getBytes();
            sendResponse(out, "HTTP/1.1 200 OK\r\n\r\n", htmlBytes, cookieValue);
        	
        } else if ("/closet".equals(uri)) {
        	System.out.println("Closet page requested");
        	
        	// HTML 파일을 읽기
        	String htmlFilePath = "./resources/detail.html";
            String htmlContent = readFile(htmlFilePath);
            
            // JSON으로부터 HTML 수정
            htmlContent = jsonToHtml(htmlContent, 2);
            
            // 바이트 리스트로 변환 후 첨부
            byte[] htmlBytes = htmlContent.getBytes();
            sendResponse(out, "HTTP/1.1 200 OK\r\n\r\n", htmlBytes, cookieValue);
        	
        } else {
        	// 잘못된 uri인 경우 404 Not Found 응답 전송
        	System.out.println("Wrong uri requested");
            sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\nFile Not Found", null, cookieValue);
        }
    }
    
    // 응답 함수
    private static void sendResponse(OutputStream out, String responseStatus, byte[] responseBody, String cookieValue) throws IOException {
    	
    	// 쿠키 없으면 생성
    	if (cookieValue == null) {
	    	System.out.println("New user requested page, cookie will be set.");
	    	int value = 2019008813; // 학번
	    	int maxAge = 60*60; // 1시간
	    	String setCookie = "Set-Cookie: StudentNumber=" + value + "; Max-Age=" + maxAge + "\r\n";
	    	String[] responseHeader = responseStatus.split("\r\n");
	    	responseStatus = responseHeader[0] +"\r\n"+ setCookie + "\r\n";
	    	if (!responseHeader[0].equals("HTTP/1.1 200 OK")) {
	    		responseStatus = responseStatus + responseHeader[2];
	    	}
    	} else {
    		// 쿠키 있으면 log 남기기
    		System.out.println("Returning user, welcome " + cookieValue);
    	}
    	// body 있으면 첨부하고 응답 보내기
		out.write(responseStatus.getBytes());
        if (responseBody != null) {
        	out.write(responseBody);
        }
        out.flush();
    }
    
    
    // 파일 읽기 함수
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
 	
	// JSON 파싱 후 detail HTML에 적용하는 함수
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
	
	// Cookie 체크
	private static String cookieCheck(BufferedReader in) throws IOException {
		
		// 클라이언트로부터의 HTTP 요청에서 쿠키 확인
		String header = in.readLine();
		while (!header.equals("")) {
//			System.out.println(header);
			if (header.startsWith("Cookie:")) {
            	String[] cookies = header.split(";");
            	for (String cookie : cookies) {
                    String[] parts = cookie.trim().split("=");
                    String name = parts[0];
                    String value = parts[1];
                    if (name.equals("Cookie: StudentNumber")) {
                    return value;
                    }
            	}
            }
			header = in.readLine();
		}
		return null;
	}
}




