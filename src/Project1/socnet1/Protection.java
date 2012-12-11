package socnet1;

import java.util.*;
public class Protection{
	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		int var = inputInt("Coiso: ", sc);
	}
	public static int inputInt(String s, Scanner sc){
		System.out.print(s);
		while(!sc.hasNextInt()){
			System.out.print(s);
			sc.next(); //limpa buffer
		}
		int i = sc.nextInt();
		sc.nextLine();  // limpar buffer para proxima leitura 
		return i;
	}

	public static float inputFloat(String s, Scanner sc){
		System.out.print(s);
		while(!sc.hasNextFloat()){
			System.out.print(s);
			sc.next(); //limpa buffer
		}
		float f = sc.nextFloat();
		sc.nextLine();  // limpar buffer para proxima leitura 
		return f;
	}
		
	public static String inputStr(String s, Scanner sc){
		String str;
		do{ 
			System.out.print(s);
			str = sc.nextLine();
		}while(str.equals(""));
		return str;
	}
}
