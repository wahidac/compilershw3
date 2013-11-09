class Test{
    public static void main(String[] a){
	int b;
	int c;
	boolean f;
	boolean g;
	boolean e;
	Jamba j;
	
	g = true && true;
	c = 540;
	j = new Jamba();
	j = (new Tootsie().returnNewJamba()).returnNewJamba();
	j = new Tootsie();
	b = j.yolo(c,g);
	j = new Tootsie();
	//b = j.roko();
	f = true;
	g = true;
	e = f && g;
	c = 3;
	while(!(b < 20)) {
		System.out.println(b);
		b = b - c;
	}
    }
}


class Jamba {
	int f1;
	boolean f2;
	int f3;
	
	public int yolo(int c, boolean b) {
		int ret;
		if(b)
			ret = this.jookie(c, !b);
		else
			ret = 100;
		return ret;
	}
	
	
	public int jookie(int a, boolean b) {
		int ret;
		if(b)
			ret = 999;
		else
			ret = a;
		return ret;
	}
	
	public int roko() {
		return 100;
	}
	
	public Jamba returnNewJamba() {
		return new Jamba();
	}
	
	public boolean mugatu() {
		return true;
	}
	
	
}

class Mokie extends Jamba {
	boolean a;
	boolean b;
	
	public boolean broko(int a) {
		return true;
	}	
	
	public int mufu() {
		return 500;
	}
	
	public boolean mufasa() {
		return false;
	}
	
	public int roko() {
		Mokie m;
		return this.mufu();
	}
}

class Tootsie extends Mokie {
	
	public int jookie(int a, boolean mojo) {
		return a;
	}
	
	public int mufu() {
		return 4;
	}
	
	public int yolo(int a, boolean b) {
		return 400;
	}
	
}

