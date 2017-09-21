package com.emotiv.dateget;
import com.emotiv.sdk.*;
public interface EngineInterface {
	
	//train
	public void trainStarted();
	public void trainSucceed();
	public void trainCompleted();
	public void trainRejected();
	public void trainErased();
	public void trainReset();
	public void userAdded(int userId);
	public void userRemove();
	
	
	// detection
	public void detectedActionLowerFace(IEE_FacialExpressionAlgo_t typeAction,float power);
}
