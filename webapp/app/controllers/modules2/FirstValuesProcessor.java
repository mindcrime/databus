package controllers.modules2;

import java.util.List;

import play.mvc.results.BadRequest;
import controllers.modules.util.OldSuccessfulAbort;
import controllers.modules2.framework.Path;
import controllers.modules2.framework.ProcessedFlag;
import controllers.modules2.framework.SuccessfulAbort;
import controllers.modules2.framework.TSRelational;
import controllers.modules2.framework.VisitorInfo;
import controllers.modules2.framework.procs.ProcessorSetup;
import controllers.modules2.framework.procs.PushProcessor;
import controllers.modules2.framework.procs.PushProcessorAbstract;

public class FirstValuesProcessor extends PushProcessorAbstract {

	private long numValues;
	private long counter = 0;
	
	@Override
	protected int getNumParams() {
		return 1;
	}

	@Override
	public String init(String pathStr, ProcessorSetup nextInChain, VisitorInfo visitor) {
		super.init(pathStr, nextInChain, visitor);
		//params = parsePath(pathStr, visitor);
		//first values may have no start and end times here
		if(params.getStart() == null) {
			long end= System.currentTimeMillis();
			//long start  = Long.MIN_VALUE;
			long start  = Long.MIN_VALUE+1; //basically, a byte is -128 to 127(long is similar) so we support -127 to -127 such that someone can negate either value and they are valid
			String previousPath = params.getPreviousPath()+"/"+start+"/"+end;
			String leftOver = params.getLeftOverPath()+"/"+start+"/"+end;
			params = new Path(params.getParams(), previousPath, leftOver, start, end, visitor.isReversed());
		}
		
		String msg = "After the /firstvalues/ in the url must be a long value of how many data points you want returned";
		numValues = parseLong(params.getParams().get(0), msg);
		return params.getLeftOverPath();
	}

	@Override
	public void incomingChunk(String url, TSRelational row, ProcessedFlag flag) {
		if(counter >= numValues) {
			throw new SuccessfulAbort("We are done processing and have enough data for first values module");
		}
		
		counter++;
		getNextInChain().incomingChunk(url, row, flag);
	}

}