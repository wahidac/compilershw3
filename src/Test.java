class Test{
    public static void main(String[] a){
	int b;
	int c;
	boolean f;
	boolean g;
	boolean e;
	Jamba j;
	
	j = new Jamba();
	j = (new Tootsie().returnNewJamba()).returnNewJamba();
	b = j.yolo();
	j = new Tootsie();
	b = j.roko();
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
	
	public int yolo() {
		
		return 200;
	}
	
	
	public int jookie(int a, boolean mojo) {
		a = 4;
		mojo = false;
		if(mojo)
			a = a + 2;
		else
			a = a + 10;
		return a;
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
	
	public int yolo() {
		return 400;
	}
	
}

