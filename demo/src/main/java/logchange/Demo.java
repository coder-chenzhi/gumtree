package logchange;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.gumtreediff.actions.ActionClusterFinder;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import org.eclipse.jdt.core.dom.ASTNode;

public class Demo {

    static Revision logChangeWithOtherLargeChurn = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\LinesOfCodeCalculator_7a89641.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\LinesOfCodeCalculator_10a8aa2.java",
            "the result is not correct by using default parameter"
    );

    static Revision addOneLineOfLog = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_5e92586601.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_745aa91a78.java"
    );

    static Revision addTwoLogWithOtherChurn = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_AlipayO2oCreateOrderController_4cdb06ced.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_AlipayO2oCreateOrderController_bdbc36819.java"
    );

    static Revision logStaticTextChange = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_CreditLoanAttributeProcessor_3b54451e5.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_CreditLoanAttributeProcessor_12945c1eb.java",
            "two log statement static text change with other non-log statement change"
    );

    static Revision deleteLogWithOtherChurn = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_JdbcBizOrderDaoForMysqlMainDb_07b566f15.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_JdbcBizOrderDaoForMysqlMainDb_7bf530e50.java",
            "delete two log statement and some non-log statement, " +
                    "the result is not correct by using default parameter"
    );

    static Revision addTwoLog = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_EntranceTracer_615a81e1e.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_EntranceTracer_6e3d7aea3.java"
    );

    static Revision twoDeleteAndTwoChange = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_5e92586601.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP_CommonPushCreditLoanTradeStatusStoreSpi_745aa91a78.java"
    );

    static Revision levelChange = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_MessagePropertyPatch_14b3ed095.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_MessagePropertyPatch_b82d772af.java"
    );

    static Revision deleteSixLogs = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_MessagePropertyPatch_442e51b59.bak.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_MessagePropertyPatch_e39a2feaa.bak.java",
            "问题有点大"
    );

    static Revision deleteOneLog = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_PayCheckResultCodeConvertor_46f6d618e.java.bak",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\TP3_PayCheckResultCodeConvertor_cbfb7e486.java.bak"
    );

    static Revision returnTest = new Revision(
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_bug\\return\\Return1.java",
            "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_bug\\return\\Return2.java"
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

    public static void getLogRevision() {
        Run.initGenerators();

        Revision test = twoDeleteAndTwoChange;
        String charset = "GB2312";
        String file1 = test.getSrc();
        String file2 = test.getDst();
        try {
            TreeContext src = new JdtTreeGenerator().setCharset(charset).generateFromFile(file1);
            TreeContext dst = new JdtTreeGenerator().setCharset(charset).generateFromFile(file2);
            Matcher m = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot()); // retrieve the default matcher
            m.match(); // mapping
            ActionGenerator g = new ActionGenerator(src.getRoot(), dst.getRoot(), m.getMappings());
            g.generate(); // generate action

            String srcText = new String (Files.readAllBytes(new File(file1).toPath()), Charset.forName(charset));
            String dstText = new String (Files.readAllBytes(new File(file2).toPath()), Charset.forName(charset));
            ITree srcTree = src.getRoot();
            ITree dstTree = dst.getRoot();
            MappingStore mapping = m.getMappings();
            List<Action> actions = g.getActions(); // return the actions

            HashMap<ITree, Action> nodeToAction = new HashMap<>();
            for (Action act : actions) {
                nodeToAction.put(act.getNode(), act);
            }

            // log addition, log deletion, log movement, log update
            // transform List<Action> to HashMap<ITree, Action>, ITree is the node operated in the Action
            // depth-first traverse the AST
            // for each methodInvocation Node, check if it is log statement
            // if it is log statement, change the boolean insideLogging to true and initialize List<String>
            // actionList to action of this methodInvocation Node (could be null)
            // traverse each Node, if insideLogging is true, add its action to List<String> actionList
            // in the postVisit(MethodInvocation Node) check if this node is loggingStatement,
            // traverse the List<Action> insideAction to determine the action of this node (add, delete, move or update)
            // finally change boolean insideLogging to false and empty List<String> actionList
            // We assume no logging statement is inside another logging statement

            // two special case
            // one is log addition, can not get by traversing old source
            // should traverse new source code
            // another is element addition in log
            // should get corresponding node in new source, and traverse it to get all addition action
            TreeUtils.visitTree(srcTree, new TreeUtils.TreeVisitor() {
                boolean insideLogging = false;
                List<String> actionsList = new ArrayList<>();
                // 感觉\\b没有必要，\b表示一个单词的边界
                Pattern p = Pattern.compile(
                        "^\\b(\\S)*log(\\S)*\\.(v|d|i|w|e|info|trace|debug|error|warn|fatal|log)\\(.*\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

                private boolean isLoggerPrintMethod(String nodeText) {
                    java.util.regex.Matcher m = p.matcher(nodeText);
                    return m.matches();
                }

                @Override
                public void startTree(ITree tree) {
                    if (!insideLogging) {
                        if (tree.getType() == ASTNode.METHOD_INVOCATION) {
                            String text = srcText.substring(tree.getPos(), tree.getEndPos());
                            if (isLoggerPrintMethod(text)) {
                                insideLogging = true;
                                if (nodeToAction.containsKey(tree)) {
                                    actionsList.add(nodeToAction.get(tree).getName());
                                } else {
                                    actionsList.add("Same");
                                }
                            }
                        }
                    } else {
                        if (nodeToAction.containsKey(tree)) {
                            actionsList.add(nodeToAction.get(tree).getName());
                        } else {
                            actionsList.add("Same");
                        }
                    }
                }

                @Override
                public void endTree(ITree tree) {
                    if (insideLogging && tree.getType() == ASTNode.METHOD_INVOCATION) {
                        String text = srcText.substring(tree.getPos(), tree.getEndPos());
                        if (isLoggerPrintMethod(text)) {
                            // handle special case for emelemt addition in log
                            // check it is not deletion
                            if (mapping.getDst(tree) != null) {
                                TreeUtils.visitTree(mapping.getDst(tree), new TreeUtils.TreeVisitor() {
                                    @Override
                                    public void startTree(ITree tree) {
                                        if (nodeToAction.containsKey(tree)) {
                                            if (nodeToAction.get(tree) instanceof Insert) {
                                                actionsList.add(nodeToAction.get(tree).getName());
                                            } else {
                                                System.err.println(nodeToAction.get(tree));
                                            }
                                        }
                                    }
                                    @Override
                                    public void endTree(ITree tree) {
                                    }
                                });
                            }
                            insideLogging = false;
                            boolean allSame = true;
                            for (int i = 0; i < actionsList.size(); i ++) {
                                if (!actionsList.get(0).equals(actionsList.get(i))) {
                                    allSame = false;
                                    break;
                                }
                            }
                            if (allSame) {
                                System.out.println(actionsList.get(0) + ":\t" + text);
                            } else {
                                System.out.println("UPD:\t" + text);
                            }
                            actionsList.clear();
                        }
                    }
                }
            });

            // handle special case for log addition
            TreeUtils.visitTree(dstTree, new TreeUtils.TreeVisitor() {

                boolean insideLogging = false;
                List<String> actionsList = new ArrayList<>();
                // 感觉\\b没有必要，\b表示一个单词的边界
                Pattern p = Pattern.compile(
                        "^\\b(\\S)*log(\\S)*\\.(v|d|i|w|e|info|trace|debug|error|warn|fatal|log)\\(.*\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

                private boolean isLoggerPrintMethod(String nodeText) {
                    java.util.regex.Matcher m = p.matcher(nodeText);
                    return m.matches();
                }

                @Override
                public void startTree(ITree tree) {
                    if (!insideLogging) {
                        if (tree.getType() == ASTNode.METHOD_INVOCATION) {
                            String text = dstText.substring(tree.getPos(), tree.getEndPos());
                            if (isLoggerPrintMethod(text)) {
                                insideLogging = true;
                                if (nodeToAction.containsKey(tree)) {
                                    if (nodeToAction.get(tree) instanceof Insert) {
                                        actionsList.add(nodeToAction.get(tree).getName());
                                    } else {
                                        System.err.println(nodeToAction.get(tree));
                                    }
                                } else {
                                    actionsList.add("NoInsert"); // maybe 'same', 'update'
                                }
                            }
                        }
                    } else {
                        if (nodeToAction.containsKey(tree)) {
                            if (nodeToAction.get(tree) instanceof Insert) {
                                actionsList.add(nodeToAction.get(tree).getName());
                            } else {
                                System.err.println(nodeToAction.get(tree));
                            }
                        }  else {
                            actionsList.add("NoInsert"); // maybe 'same', 'update'
                        }
                    }
                }

                @Override
                public void endTree(ITree tree) {
                    if (insideLogging && tree.getType() == ASTNode.METHOD_INVOCATION) {
                        String text = dstText.substring(tree.getPos(), tree.getEndPos());
                        if (isLoggerPrintMethod(text)) {

                            insideLogging = false;
                            boolean allInsert = true;
                            for (int i = 0; i < actionsList.size(); i ++) {
                                if (!actionsList.get(i).equals("INS")) {
                                    allInsert = false;
                                    break;
                                }
                            }
                            if (allInsert) {
                                System.out.println("INS:\t" + text);
                            }
                            actionsList.clear();
                        }
                    }
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void getProgrammableResult() {
        Run.initGenerators();

        Revision test = addTwoLog;
        String file1 = test.getSrc();
        String file2 = test.getDst();
        try {
            TreeContext src = new JdtTreeGenerator().setCharset("UTF-8").generateFromFile(file1);
            TreeContext dst = new JdtTreeGenerator().setCharset("UTF-8").generateFromFile(file2);
            Matcher m = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot()); // retrieve the default matcher
            m.match();
            ActionGenerator g = new ActionGenerator(src.getRoot(), dst.getRoot(), m.getMappings());
            g.generate();
            List<Action> actions = g.getActions(); // return the actions
//            for (Action act : actions) {
//                System.out.println(act.toString());
//            }
            ActionClusterFinder f = new ActionClusterFinder(src, dst, actions);
            for (Set<Action> cluster : f.getClusters()) {
                System.out.println("New cluster:");
                System.out.println(f.getClusterLabel(cluster));
                System.out.println("------------");
                for (Action a : cluster)
                    System.out.println(a.format(src));
                System.out.println("");
            }

        } catch (UnsupportedOperationException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void testDiffCluster() {
        Revision adjacentAdditon = new Revision(
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\test_cluster\\Simple1.java",
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\test_cluster\\Simple2.java"
        );

        Revision notAdjacentAdditon = new Revision(
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\test_cluster\\Simple1.java",
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\test_cluster\\Simple3.java"
        );

        Revision staticTextChangeAndVarChange = new Revision(
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\generated\\Origin.java",
                "E:\\Code\\GumTreeSpace\\gumtree\\test_data\\src_log_change\\generated\\StaticTextChangeAndVariableChange.java"
        );

        Run.initGenerators();

        Revision test = staticTextChangeAndVarChange;
        String file1 = test.getSrc();
        String file2 = test.getDst();
        try {
            TreeContext src = new JdtTreeGenerator().setCharset("UTF-8").generateFromFile(file1);
            TreeContext dst = new JdtTreeGenerator().setCharset("UTF-8").generateFromFile(file2);
            Matcher m = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot()); // retrieve the default matcher
            m.match();
            ActionGenerator g = new ActionGenerator(src.getRoot(), dst.getRoot(), m.getMappings());
            g.generate();
            List<Action> actions = g.getActions(); // return the actions
//            for (Action act : actions) {
//                System.out.println(act.toString());
//            }
            ActionClusterFinder f = new ActionClusterFinder(src, dst, actions);
            for (Set<Action> cluster : f.getClusters()) {
                System.out.println("New cluster:");
                System.out.println(f.getClusterLabel(cluster));
                System.out.println("------------");
                for (Action a : cluster)
                    System.out.println(a.format(src));
                System.out.println("");
            }

        } catch (UnsupportedOperationException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void runClient() {
        Revision test = twoDeleteAndTwoChange;
        String[] args = new String[]{
//                "list", "MATCHERS"
                "-c", "gt.charset.decoding", "GB2312", "webdiff",
//                "-m", "change-distiller",
                test.getSrc(), test.getDst()
        };
        Run.main(args);
    }

    public static void main(String[] args) {
//        getProgrammableResult();
        getLogRevision();
        runClient();
//        testDiffCluster();
    }
}
