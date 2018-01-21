import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test2 {
	static final Logger logger = LoggerFactory.getLogger(Test2.class);

	public static void main() {

	}

	public static void logLevelChange() {
		logger.info("test");
	}

	public static void logStaticTextChange() {
		logger.info("test");
	}

	public static void logStaticTextChange2() {
		logger.info("You know anything, John Snow.", "Winter is coming");
	}

	public static void logVaribleChange() {
		int a = 0;
		logger.info("test", a);
	}
	
	public static void logVaribleChange2() {
		int a = 0;
		logger.info("test", a);
	}

	public static void logMove() {
		if (true) {
			logger.info("test");
		} else {
			
		}
	}
	
	public static void logMove2() {
		int a = 1, b = 2;
		a = a + b;
		logger.info("test");
	}
	
	public static void logAddition() {

	}

	public static void logDeletion() {
		logger.info("test");
	}
	
	public static void logAdditionAndStaticTextChange() {
		// 这个case，ChangeDistiller的结果并不对
		if (true) {
			logger.info("logAdditionAndStaticTextChange");
		}
	}

}