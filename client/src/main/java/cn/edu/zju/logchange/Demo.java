package cn.edu.zju.logchange;

import java.io.IOException;
import java.util.List;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

public class Demo {

	public static void main(String[] args) {
		Run.initGenerators();
		String file1 = "file_v0.java";
		String file2 = "file_v1.java";
		ITree src;
		try {
			src = Generators.getInstance().getTree(file1).getRoot();
			ITree dst = Generators.getInstance().getTree(file2).getRoot();
			Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
			m.match();
			ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
			g.generate();
			List<Action> actions = g.getActions(); // return the actions
			
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
