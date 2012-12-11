package socnet1;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JDialog;

public class PopUp{

	@SuppressWarnings("deprecation")
	public static void main(String args[]){

		JOptionPane pane = new JOptionPane( toSpaces(args[0]), Integer.parseInt(args[1]) );

		JDialog dialog = pane.createDialog("PopUp");
		dialog.resize(400,200);
		dialog.setVisible(true);

		if( (Integer) pane.getValue()==0 )
			System.exit(0);

	}


	private static String toSpaces(String s){
		int i; String res = "";
		for( i = 0; i < s.length(); ++i )
			res += s.charAt(i)=='_' ? ' ' : s.charAt(i);
		return res;
	}
}
