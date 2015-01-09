package se.mju.stackanalyzer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.junit.Test;

import se.mju.stackanalyzer.model.StackTrace;
import se.mju.stackanalyzer.model.ThreadState;
import se.mju.stackanalyzer.util.Tuple;

public class StackTracesStatisticsTest {
	private static final String traces = 
			"\"threadName1\" daemon prio=10 tid=0x01 nid=0x01 runnable [0x01]\n" + 
			"   java.lang.Thread.State: RUNNABLE\n" + 
			"	at se.mju.test.Fake.method1(Fake.java:2)\n" + 
			"	at se.mju.test.Fake.main(Fake.java:1)\n" +
			"\n" +
			"\"threadName1\" daemon prio=10 tid=0x01 nid=0x01 runnable [0x01]\n" + 
			"   java.lang.Thread.State: RUNNABLE\n" + 
			"	at se.mju.test.Fake.method1(Fake.java:2)\n" + 
			"	at se.mju.test.Fake.main(Fake.java:1)\n" +
			"\n" +
			"\"threadName1\" daemon prio=10 tid=0x01 nid=0x01 runnable [0x01]\n" + 
			"   java.lang.Thread.State: RUNNABLE\n" + 
			"	at se.mju.test.Fake.main(Fake.java:1)\n" +
			"\n" +
			"\"threadName1\" daemon prio=10 tid=0x01 nid=0x01 runnable [0x01]\n" + 
			"   java.lang.Thread.State: RUNNABLE\n" + 
			"	at se.mju.test.Fake.method2(Fake.java:3)\n" + 
			"	at se.mju.test.FakeFilter.method(FakeFilter.java:1)\n" + 
			"	at se.mju.test.Fake.main(Fake.java:1)\n";


	@Test
	public void methodStatistics() throws IOException {
		// Given
		List<ThreadState> parsed = new ThreadDumpParser(traces).parseAll();
		StackTracesStatistics stackTracesStatistics = new StackTracesStatistics(parsed);
		
		// When
		SortedSet<Tuple<StackTrace,Float>> roots = stackTracesStatistics.getStatForChildrens(null);
		SortedSet<Tuple<StackTrace,Float>> level2 = stackTracesStatistics.getStatForChildrens(roots.first().getFirst());
		
		// Then
		assertEquals(1, roots.size());
		assertEquals(1.0, roots.first().getSecond().floatValue(), 0.01);
		
		assertEquals(2, level2.size());
		assertEquals(0.50, level2.first().getSecond().floatValue(), 0.01);
		assertEquals(0.25, level2.last().getSecond().floatValue(), 0.01);
	}

	@Test
	public void filter() throws IOException {
		// Given
		List<ThreadState> parsed = new ThreadDumpParser(traces).parseAll();
		
//		// When
		List<ThreadState> filtered = parsed.stream()
				.map(threadState -> threadState.filtered( e -> !e.getClassName().equals("se.mju.test.FakeFilter")))
				.collect(Collectors.toList());
		
		// Then
		StackTracesStatistics stackTracesStatistics = new StackTracesStatistics(filtered);
		SortedSet<Tuple<StackTrace,Float>> roots = stackTracesStatistics.getStatForChildrens(null);
		SortedSet<Tuple<StackTrace,Float>> level2 = stackTracesStatistics.getStatForChildrens(roots.first().getFirst());

		assertEquals("se.mju.test.Fake", level2.first().getFirst().getClassName());
		assertEquals("se.mju.test.Fake",level2.last().getFirst().getClassName() );
	}
}
