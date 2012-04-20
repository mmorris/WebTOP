package org.webtop.module.polarization;

import java.util.LinkedList;
import java.util.ListIterator;


public class FilterList extends LinkedList<Filter> {
	ListIterator<Filter> curr;
	Filter currentFilter;
	
	public FilterList() {
		curr = listIterator();
		currentFilter = null;
	}
	
	public void removeAll() {
		while(size()>0)
			removeFirst();
		
		currentFilter = null;
		curr = listIterator();
	}

	public void insertAfter(Filter p, Filter p0) {
		int index = indexOf(p);
		if(index == -1)
			return;
		add(index+1,p0);
	} 

	
	public int indexOfPolarizer(Filter p) {
		return indexOf(p);
	}

	public void move(Filter f, float z_, boolean setVRML) {
		if(!contains(f)) return;
		
		remove(f);
		f.setZ(z_, setVRML);
		
		ListIterator<Filter> iter = listIterator(0);
		Filter f2;
		f2 = iter.next();
		while(iter.hasNext() && f.getZ() < f2.getZ()) {
			f2 = iter.next();
		}
		add(indexOf(f2),f);
	}

	public void hideWidgets() {
		ListIterator<Filter> iter = listIterator(0);
		Filter f;
		while(iter.hasNext()) {
			f = iter.next();
			f.setActive(false);
		}
	}

	public void setEnabled(boolean on) {
		ListIterator<Filter> iter = listIterator(0);
		Filter f;
		while(iter.hasNext()) {
			f = iter.next();
			f.setEnabled(on);
		}
	}

	public void setAngle(Filter p, float angle_, boolean setVRML) {
		if(!contains(p)) return;
		p.setAngle(angle_, setVRML);
	}

	public Filter getFilter(int index) {
		return get(index);
	}

	public Filter getFilter(String id) {
		if(id==null) return null;
		
		ListIterator<Filter> iter = listIterator(0);
		Filter f;
		while(iter.hasNext()) {
			f = iter.next();
			if(id.equals(f.getID())) return f;
		}
		return null;
	}
	
	public void printList() {
		ListIterator<Filter> iter = listIterator(0);
		Filter f;
		while(iter.hasNext()) {
			f = iter.next();
			//DebugPrinter.println("Z: " + p.getZ() + ", Angle: " + p.getAngle());
		}
	}

	public void printReverseList() {
		ListIterator<Filter> iter = listIterator(size()-1);
		Filter f;
		while(iter.hasPrevious()) {
			f = iter.previous();
			//DebugPrinter.println("Z: " + p.getZ() + ", Angle: " + p.getAngle());
		}
	}
	
	//TODO: check these functions out
	public Filter current() {
		return currentFilter;
	}

	public Filter prev() {
		if(!curr.hasPrevious())
			return null;
		
		currentFilter = curr.previous();
		return currentFilter;
	}

	public Filter next() {	
		if(!curr.hasNext())
			return null;
		
		currentFilter = curr.next();
		return currentFilter;
		
	}

	public Filter first() {
		curr = listIterator();
		
		currentFilter = curr.next();
		return currentFilter;
	}

	public Filter last() {
		curr = listIterator(size()-1);
		
		currentFilter = curr.next();
		return currentFilter;
	}

}
