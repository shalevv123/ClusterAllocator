import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// ************************************************************************
// This is a run example
// ************************************************************************

import org.optaplanner.allocatorbalancing.app.*;
public class Run {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        for (int time = 2; time<5 ; time+=1) {
            AllocatorBalancingHandler.Solve(AllocatorBalancingHandler.getBalance("./data/unsolved/2clusters-9servers.xml"), time,
                    true, true, "./data/solved/test" + time +".xml", true);
        }

        AllocatorBalancingHandler.graphResults(true,"./pictures/graphTest.png");
    }
}
