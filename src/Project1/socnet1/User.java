package socnet1;

import java.util.Stack;
import java.io.Serializable;

public class User implements Serializable{
    private String username;
    private String password;

    // PM's cuja notificação já foi enviada, mas ainda não foram lidas
    private Stack<Integer> pmsToBeRead;     // only ids
    private Stack<Integer> postsToBeRead;   // only ids

    public User( String user, String password ){
        this.username = user;
        this.password = password;

        pmsToBeRead   = new Stack<Integer>();
        postsToBeRead = new Stack<Integer>();
    }


    public Stack<Integer> getPmsToBeRead(){
    	return pmsToBeRead;
    }

    public Stack<Integer> getPostsToBeRead(){
    	return postsToBeRead;
    }

    public String getPassword(){
    	return password;
    }

    public void pushPmToBeRead(int id){
    	pmsToBeRead.push(id);
    }

    public void pushPostToBeRead(int id){
    	postsToBeRead.push(id);
    }

    public int popPostToBeRead(){
        if( !postsToBeRead.isEmpty() )
            return postsToBeRead.pop();
        return -1;
    }
}