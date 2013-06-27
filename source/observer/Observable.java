package observer;


import java.util.ArrayList;
import java.util.Iterator;

public class Observable {

	private ArrayList<IObserver> subscribers = new ArrayList<IObserver>(2);

	public void addObserver(IObserver s){
		subscribers.add(s);
	}

	public void removeObserver(IObserver s){
		subscribers.remove(s);
	}

	public void removeAllObservers(IObserver s){
		subscribers.clear();

	}
	//neu
	public void notifyObserver(IObserver s){
		subscribers.notify();
	}

	public void notifyAllObserver(IObserver s){
		subscribers.notifyAll();
	}

	public void notifyChoosePlayer(){
		for(Iterator<IObserver> iter = subscribers.iterator(); iter.hasNext();){
			IObserver observer = iter.next();
		}
	}
	
	public void notifyChooseStone(){
		for(Iterator<IObserver> iter = subscribers.iterator(); iter.hasNext();){
			IObserver observer = iter.next();
		}
	}
	
	public void notifyChangePlayer(){
		for(Iterator<IObserver> iter = subscribers.iterator(); iter.hasNext();){
			IObserver observer = iter.next();
		}
	}


}