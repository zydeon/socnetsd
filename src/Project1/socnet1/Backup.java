package socnet1;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Backup {	

	private static final String FILES_PATH         = "./";
	
	private static final String USERS_FILE         = FILES_PATH+"users.obj";
	private static final String PMS_FILE           = FILES_PATH+"pms.obj";
	private static final String POSTS_FILE         = FILES_PATH+"posts.obj";
	private static final String DELAYED_POSTS_FILE = FILES_PATH+"delayed_posts.obj";

	private ObjectInputStream iS;
	private ObjectOutputStream oS;
	
	private void abreLeitura(String nomeDoFicheiro) throws IOException {
		iS = new ObjectInputStream(new FileInputStream(nomeDoFicheiro));
	}
	private void abreEscrita(String nomeDoFicheiro) throws IOException{
		oS = new ObjectOutputStream(new FileOutputStream(nomeDoFicheiro));
	}
	private Object leObjecto() throws IOException,ClassNotFoundException{
		return iS.readObject();	
	}
	private void escreveObjecto(Object o) throws IOException{
		oS.writeObject(o);
	}
	private void fechaLeitura() throws IOException{
		iS.close();
	}
	private void fechaEscrita() throws IOException{
		oS.close();
	}
	
	@SuppressWarnings("unchecked")	
	public static ConcurrentHashMap<String, User> readUsers() {
		Backup b = new Backup();		
		ConcurrentHashMap<String, User> users;
		try{
			b.abreLeitura(USERS_FILE);
			users = (ConcurrentHashMap<String, User>) b.leObjecto();
			b.fechaLeitura();

			return users;							
		}
		catch (EOFException e){System.out.println("ERRO "+USERS_FILE+" "+e.getMessage());}
		catch (FileNotFoundException e){/*System.out.println("ERRO "+USERS_FILE+" "+e.getMessage());*/}
		catch (java.io.IOException e){System.out.println("ERRO "+USERS_FILE+" "+e.getMessage());}
		catch (java.lang.ClassNotFoundException e){System.out.println("ERRO "+USERS_FILE+" "+e.getMessage());}		
		
		return null;
	}

	@SuppressWarnings("unchecked")	
	public static ConcurrentHashMap<Integer, PM> readPMs() {
		Backup b = new Backup();		
		ConcurrentHashMap<Integer, PM> pms;
		try{
			b.abreLeitura(PMS_FILE);
			pms = (ConcurrentHashMap<Integer, PM>) b.leObjecto();
			b.fechaLeitura();

			return pms;							
		}
		catch (EOFException e){System.out.println("ERRO "+PMS_FILE+" "+e.getMessage());}
		catch (FileNotFoundException e){System.out.println("ERRO "+PMS_FILE+" "+e.getMessage());}
		catch (java.io.IOException e){System.out.println("ERRO "+PMS_FILE+" "+e.getMessage());}
		catch (java.lang.ClassNotFoundException e){System.out.println("ERRO "+PMS_FILE+" "+e.getMessage());}		
		
		return null;
	}	
	
	@SuppressWarnings("unchecked")	
	public static ConcurrentHashMap<Integer, Post> readPosts() {
		Backup b = new Backup();		
		ConcurrentHashMap<Integer, Post> posts;
		try{
			b.abreLeitura(POSTS_FILE);
			posts = (ConcurrentHashMap<Integer, Post>) b.leObjecto();
			b.fechaLeitura();

			return posts;							
		}
		catch (EOFException e){System.out.println("ERRO "+POSTS_FILE+" "+e.getMessage());}
		catch (FileNotFoundException e){System.out.println("ERRO "+POSTS_FILE+" "+e.getMessage());}
		catch (java.io.IOException e){System.out.println("ERRO "+POSTS_FILE+" "+e.getMessage());}
		catch (java.lang.ClassNotFoundException e){System.out.println("ERRO "+POSTS_FILE+" "+e.getMessage());}		
		
		return null;
	}

	@SuppressWarnings("unchecked")	
	public static ConcurrentHashMap<Integer, DelayedPost> readDelayedPosts() {
		Backup b = new Backup();		
		ConcurrentHashMap<Integer, DelayedPost> posts;
		try{
			b.abreLeitura(DELAYED_POSTS_FILE);
			posts = (ConcurrentHashMap<Integer, DelayedPost>) b.leObjecto();
			b.fechaLeitura();

			return posts;							
		}
		catch (EOFException e){System.out.println("ERRO "+DELAYED_POSTS_FILE+" "+e.getMessage());}
		catch (FileNotFoundException e){System.out.println("ERRO "+DELAYED_POSTS_FILE+" "+e.getMessage());}
		catch (java.io.IOException e){System.out.println("ERRO "+DELAYED_POSTS_FILE+" "+e.getMessage());}
		catch (java.lang.ClassNotFoundException e){System.out.println("ERRO "+DELAYED_POSTS_FILE+" "+e.getMessage());}		
		
		return null;
	}	

	public static void writeUsers( ConcurrentHashMap<String, User> users ){
		Backup b = new Backup();		
		try{
			b.abreEscrita(USERS_FILE);
			b.escreveObjecto(users);
			b.fechaEscrita();
		}
		catch (FileNotFoundException e) {System.out.println("Ficheiro nao existente: "+e.getMessage());}
		catch (IOException e){System.out.println("Erro de I/O: "+e.getMessage());}		

	}

	public static void writePms( ConcurrentHashMap<Integer, PM> pms ){		
		Backup b = new Backup();		
		try{
			b.abreEscrita(PMS_FILE);
			b.escreveObjecto(pms);
			b.fechaEscrita();
		}
		catch (FileNotFoundException e) {System.out.println("Ficheiro nao existente: "+e.getMessage());}
		catch (IOException e){System.out.println("Erro de I/O: "+e.getMessage());}		

	}

	public static void writePosts( ConcurrentHashMap<Integer, Post> posts ){
		Backup b = new Backup();		
		try{
			b.abreEscrita(POSTS_FILE);
			b.escreveObjecto(posts);
			b.fechaEscrita();
		}
		catch (FileNotFoundException e) {System.out.println("Ficheiro nao existente: "+e.getMessage());}
		catch (IOException e){System.out.println("Erro de I/O: "+e.getMessage());}		

	}		

	public static void writeDelayedPosts( ConcurrentHashMap<Integer, DelayedPost> dps ){
		Backup b = new Backup();		
		try{
			b.abreEscrita(DELAYED_POSTS_FILE);
			b.escreveObjecto(dps);
			b.fechaEscrita();
		}
		catch (FileNotFoundException e) {System.out.println("Ficheiro nao existente: "+e.getMessage());}
		catch (IOException e){System.out.println("Erro de I/O: "+e.getMessage());}		

	}	

}
