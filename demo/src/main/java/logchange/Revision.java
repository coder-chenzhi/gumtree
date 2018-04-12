package logchange;

/**
 * Created by chenzhi on 2018/4/12.
 */
public class Revision {
    private String src;
    private String dst;
    private String description = "";

    public Revision(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    public Revision(String src, String dst, String description) {
        this.src = src;
        this.dst = dst;
        this.description = description;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Revision{" +
                "src='" + src + '\'' +
                ", dst='" + dst + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
