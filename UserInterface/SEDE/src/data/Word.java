/*
Copyright (c) 2014 High-Performance Computing and GIS (HPCGIS) Laboratory. All rights reserved.
Use of this source code is governed by a BSD-style license that can be found in the LICENSE file.
Authors and contributors: Jayakrishnan Ajayakumar (jajayaku@kent.edu);Eric Shook (eshook@kent.edu)
*/
package data;
//Pojo class to hold a word with count
public class Word implements Comparable<Word>{
	private String word;
	private Integer count;
	@Override
	public int compareTo(Word other) {
		return -(this.count.compareTo(other.getCount()));
	}
	public Word(String word, int count) {
		this.word = word;
		this.count = count;
	}
	public String getWord() {
		return word;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	

}
