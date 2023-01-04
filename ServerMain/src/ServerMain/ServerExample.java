package ServerMain;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerExample {
	ServerSocket serverSocket = null; //서버소켓
	Socket socket = null; //소켓
	Vector<Clients> vec = new Vector<Clients>(); //접속자
	
	public static void main(String[] args) {
		new ServerExample(args);
	}

	ServerExample(String[] args) {
		try {
			int serverport = 8000;
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(serverport));
			System.out.println("[server active...]");
			while (true) {
				//client와의 연결 대기
				System.out.println("[connection waiting...]");
				socket = serverSocket.accept(); //소켓에 서버 소켓이 accept
				Clients clients = new Clients(socket); //client와의 통신을 담당할 스레드클래스 생성
				vec.addElement(clients); //벡터클래스의 addElement 메서드를 사용해 벡터에 추가
				System.out.println("현재 접속자 수 = " + (vec.size()));
				clients.start();
			}
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
	
	class Clients extends Thread{
		InputStream is;
		OutputStream os;
		Socket socket;
		byte[] bytes = null;
		String message = null;
		
		public Clients(Socket socket)
		{
			this.socket = socket;
			try {
				is = socket.getInputStream(); //데이터 수신객체
				os = socket.getOutputStream(); //데이터 송신객체
			}
			catch (IOException e) {
				System.err.print("client error : " + e);
			}
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					bytes = new byte[200]; //상대방이 보낸 데이터를 받기 위해 데이터를 저장할 byte 배열 생성
					int readByteCount = is.read(bytes); //inputStream의 read() 메소드 호출
					message = new String(bytes, 0, readByteCount, "UTF-8"); //parameter -> bytes, offset, readByteCount, charsetName
					System.out.println("입력받은 내용 : " + message);
				}
				catch (IOException e) {
					System.out.println("입력 에러 : " + e);
					Quit(this); //parameter -> clients
					return;
				}
				Broadcast(message); //서버가 받은 메세지를 모든 client에게 보냄
			}
		}
		
		public void Broadcast(String message) {
			int size = vec.size();
			for(int i = 0; i < size; i++) {
				//vec 배열에 있는 client에게 메세지 발송
				try {
					Clients clients = (Clients) vec.elementAt(i);
					byte[] bytes = message.getBytes("UTF-8"); //byte배열에 메세지를 UTF-8로 인코딩하여 발송 parameter -> charsetName
					clients.os.write(bytes);
					clients.os.flush(); //발송
					System.out.println(bytes + " 데이터 보내기 성공"); //발송된 데이터의 내용과 데이터 발송성공 여부 확인
				}
				catch(IOException e) {
					System.err.println(e);
				}
			}
		}
		
		public void Quit(Clients clients) {
			int tmp = vec.indexOf(clients);
			if(tmp >= 0) {
				vec.removeElementAt(tmp);
				try {
					clients.is.close();
					clients.os.close();
					clients.socket.close();
					System.out.println("현재 접속자 수 = " + (vec.size()));
				}
				catch (IOException e) {
					System.out.println("현재 접속자 수 = " + (vec.size()));
					System.err.println(e);
				}
			}
			else {
				System.out.println("Quit 처리 에러");
			}
		}
	}
}


