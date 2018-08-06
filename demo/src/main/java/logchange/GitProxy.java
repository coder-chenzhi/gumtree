package logchange;

import com.github.gumtreediff.utils.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenzhi on 2018/4/16.
 */
public class GitProxy {

    private Git git;

    public Git getGit() {
        return git;
    }

    /**
     * initialize by an existing repository
     *
     * @param dir
     */
    public GitProxy(String dir) {
        try {
            // To open an existing repo
            this.git = Git.open(new File(dir));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize by cloning an repository
     *
     * @param url clone url
     * @param dir local dir
     */
    public GitProxy(String url, String dir) {
        try {
            // Cloning the repo
            this.git = Git.cloneRepository().setURI(url).setDirectory(new File(dir)).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RevCommit> getTopoOrderCommits() { //FIXME not identical to 'git log --topo-order' result
        ArrayList<RevCommit> commits = new ArrayList<>();
        Repository repo = git.getRepository();
        RevWalk walk = new RevWalk(repo);
        try {
            AnyObjectId headId = repo.resolve(Constants.HEAD);
            RevCommit root = walk.parseCommit(headId);
            walk.sort(RevSort.TOPO);
            walk.markStart(root);
            for (Iterator<RevCommit> iterator = walk.iterator(); iterator.hasNext(); ) {
                RevCommit commit = iterator.next();
                commits.add(commit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return commits;
    }

    public List<DiffEntry> getDiffBetweenFirstParent(RevCommit commit) throws IOException, GitAPIException {
        if (commit.getParentCount() == 0) {
            return null;
        }

        Repository repo = git.getRepository();
        RevWalk rw = new RevWalk(repo);
        rw.setRetainBody(false);

        RevCommit firstParent = commit.getParent(0);
        firstParent = rw.parseCommit(firstParent.getId());
        rw.dispose();

        List<DiffEntry> diff = git.diff().
                setOldTree(prepareTreeParser(repo, firstParent)).
                setNewTree(prepareTreeParser(repo, commit)).
                setPathFilter(PathSuffixFilter.create(".java")).
                call();

        List<DiffEntry> newDiff = new ArrayList<>();
        for (DiffEntry entry : diff) {
            if (!entry.getOldPath().contains("src/test/java") && !entry.getNewPath().contains("src/test/java")) {
                newDiff.add(entry);
            }
        }
        return newDiff;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, RevCommit commit) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }


    // 按拓扑结构遍历所有commit（default branch reachable commit）
    // 获取每个commit与其first parent commit的变更（需要过滤测试文件和非Java文件）
    // 获取每个文件变更前后的内容，然后调用FineLevelLogChange

    public static void main(String[] args) {
        String dir = "E:\\Code\\tmp\\druid";

        GitProxy gitProxy = new GitProxy(dir);
        List<RevCommit> commits = gitProxy.getTopoOrderCommits();
        RevCommit commit = commits.get(0);
        System.out.println(commit.getId());
        Repository repo = gitProxy.getGit().getRepository();
        ObjectReader reader = repo.newObjectReader();
        try {
            List<DiffEntry> diffs = gitProxy.getDiffBetweenFirstParent(commit);
            if (diffs != null) {
                for (DiffEntry diff : diffs) {
                    ObjectId objectId = diff.getNewId().toObjectId();
                    byte[] bytes = reader.open(objectId).getBytes();
                    System.out.println(new String(bytes, "utf-8"));
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
    }

}
