package com.emotiv.getdata;
import com.emotiv.sdk.*;
public interface EngineInterface {
	//train
	public void trainStarted();
	public void trainSucceed();
	public void trainFailed();
	public void trainCompleted();
	public void trainRejected();
	public void trainReset();
	public void trainErased();
	public void userAdd(int userId);
	public void userRemoved();

	//action
	public void currentAction(IEE_MentalCommandAction_t typeAction,float power);

}
