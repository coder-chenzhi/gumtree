import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test2 {
	static final Logger logger = LoggerFactory.getLogger(Test2.class);
	public static void main() {
		
	}
	
	public static void logLevelChange() {
		logger.warn("test");
	}
	
	public static void logStaticTextChange() {
		logger.info("test2");
	}
	
	public static void logStaticTextChange2() {
		logger.info("You know anything, John Snow.");
	}
	
	public static void logVaribleChange() {
		int a = 0;
		logger.info("test");
	}
	
	public static void logVaribleChange2() {
		int b = 0;
		logger.info("test");
	}
	
	public static void logMove() {
		if (true) {
			
		} else {
			logger.info("test");
		}
	}
	
	public static void logMove2() {
		int a = 1, b = 2;
		logger.info("test");
		a = a + b;
	}
	
	public static void logAddition() {
		logger.info("test2");
	}
	
	public static void logDeletion() {
		
	}
	
	public static void logAdditionAndStaticTextChange() {
		// 这个case，ChangeDistiller的结果并不对
		logger.info("logAdditionAndStaticTextChange start");
		if (true) {
			logger.info("inside logAdditionAndStaticTextChange");
		}
	}
}