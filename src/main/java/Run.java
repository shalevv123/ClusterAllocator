import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.allocatorbalancing.app.*;
public class Run {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        for (int time = 2; time<5 ; time+=1) {
            AllocatorBalancingHandler.Solve("2clusters-9servers", time, true,
                    true, "test_"+time+" Seconds", true);
        }
        AllocatorBalancingHandler.graphResults(true,"graphTest");
    }
}
