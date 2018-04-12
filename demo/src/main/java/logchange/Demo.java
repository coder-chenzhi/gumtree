package logchange;

import java.io.IOException;
import java.util.List;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;

public class Demo {

    static Revision logChangeWithOtherLargeChurn = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\LinesOfCodeCalculator_7a89641.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\LinesOfCodeCalculator_10a8aa2.java"
    );

    static Revision addOneLineOfLog = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_5e92586601.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_745aa91a78.java"
    );

    static Revision logStaticTextChange = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_CreditLoanAttributeProcessor_3b54451e5.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_CreditLoanAttributeProcessor_12945c1eb.java",
            "two log statement static text change with other non-log statement change"
    );

    public static void JDTParse(String file1, String file2) {
        ITree src;
        try {
            src = new JdtTreeGenerator().generateFromFile(file1).getRoot();
            ITree dst = new JdtTreeGenerator().generateFromFile(file2).getRoot();
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


    public static void getProgrammableResult() {
        String s = new String();
        Run.initGenerators();

        Revision test = addOneLineOfLog;
        String file1 = test.getSrc();
        String file2 = test.getDst();
        ITree src;
        try {
            src = new JdtTreeGenerator().setEncoding("GB2312").generateFromFile(file1).getRoot();
            ITree dst = new JdtTreeGenerator().setEncoding("GB2312").generateFromFile(file2).getRoot();
            Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
            m.match();
            ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
            g.generate();
            List<Action> actions = g.getActions(); // return the actions
            for (Action act : actions) {
                System.out.println(act.toString());
            }

        } catch (UnsupportedOperationException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void runClient() {
        Revision test = addOneLineOfLog;
        String[] args = new String[]{
                "-c", "gt.encoding.default", "GB2312",
                "webdiff", test.getSrc(), test.getDst()
        };
        Run.main(args);
    }

    public static void main(String[] args) {
//        getProgrammableResult();
        runClient();
    }
}
