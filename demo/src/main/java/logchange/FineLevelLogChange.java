package logchange;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by chenzhi on 2018/4/16.
 */
public class FineLevelLogChange {

    class SameAction extends Action{

        public SameAction(ITree node) {
            super(node);
        }

        @Override
        public String getName() {
            return "Same";
        }
        @Override
        public String toString() {
            return "Same";
        }
        @Override
        public String format(TreeContext ctx) {
            return "Same";
        }
    }

    class NoInsertAction extends Action{

        public NoInsertAction(ITree node) {
            super(node);
        }

        @Override
        public String getName() {
            return "NoInsert";
        }
        @Override
        public String toString() {
            return "NoInsert";
        }
        @Override
        public String format(TreeContext ctx) {
            return "NoInsert";
        }
    }

    Pattern p = Pattern.compile(
            "^\\b(\\S)*log(\\S)*\\.(v|d|i|w|e|info|trace|debug|error|warn|fatal|log)\\(.*\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private boolean isLoggerPrintMethod(String nodeText) {
        java.util.regex.Matcher m = p.matcher(nodeText);
        return m.matches();
    }

    private String fileToString(String filePath, String[] charsets) {
        return "";
    }

    public void getLogRevision(String srcText, String dstText) {
        Run.initGenerators();

        try {
            TreeContext src = new JdtTreeGenerator().generateFromString(srcText);
            TreeContext dst = new JdtTreeGenerator().generateFromString(dstText);
            Matcher m = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot()); // retrieve the default matcher
            m.match(); // mapping
            ActionGenerator g = new ActionGenerator(src.getRoot(), dst.getRoot(), m.getMappings());
            g.generate(); // generate action

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
                List<Action> actionsList = new ArrayList<>();

                @Override
                public void startTree(ITree tree) {
                    if (!insideLogging) {
                        if (tree.getType() == ASTNode.METHOD_INVOCATION) {
                            String text = srcText.substring(tree.getPos(), tree.getEndPos());
                            if (isLoggerPrintMethod(text)) {
                                insideLogging = true;
                                if (nodeToAction.containsKey(tree)) {
                                    actionsList.add(nodeToAction.get(tree));
                                } else {
                                    actionsList.add(new SameAction(tree));
                                }
                            }
                        }
                    } else {
                        if (nodeToAction.containsKey(tree)) {
                            actionsList.add(nodeToAction.get(tree));
                        } else {
                            actionsList.add(new SameAction(tree));
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
                                                actionsList.add(nodeToAction.get(tree));
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
                            for (Action act : actionsList) {
                                if (!actionsList.get(0).getName().equals(act.getName())) {
                                    allSame = false;
                                    break;
                                }
                            }
                            if (allSame) {
                                System.out.println(actionsList.get(0).getName() + ":\t" + text);
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
                List<Action> actionsList = new ArrayList<>();

                @Override
                public void startTree(ITree tree) {
                    if (!insideLogging) {
                        if (tree.getType() == ASTNode.METHOD_INVOCATION) {
                            String text = dstText.substring(tree.getPos(), tree.getEndPos());
                            if (isLoggerPrintMethod(text)) {
                                insideLogging = true;
                                if (nodeToAction.containsKey(tree)) {
                                    if (nodeToAction.get(tree) instanceof Insert) {
                                        actionsList.add(nodeToAction.get(tree));
                                    } else {
                                        System.err.println(nodeToAction.get(tree));
                                    }
                                } else {
                                    actionsList.add(new NoInsertAction(tree)); // maybe 'same', 'update'
                                }
                            }
                        }
                    } else {
                        if (nodeToAction.containsKey(tree)) {
                            if (nodeToAction.get(tree) instanceof Insert) {
                                actionsList.add(nodeToAction.get(tree));
                            } else {
                                System.err.println(nodeToAction.get(tree));
                            }
                        }  else {
                            actionsList.add(new NoInsertAction(tree)); // maybe 'same', 'update'
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
                            for (Action act : actionsList) {
                                if (!act.getName().equals(new Insert(null, null, 0).getName())) {
                                    allInsert = false;
                                    break;
                                }
                            }
                            if (allInsert) {
                                System.out.println(new Insert(null, null, 0).getName() + ":\t" + text);
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



}
