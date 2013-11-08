class Test{
    public static void main(String[] a){
	int b;
	int c;
	boolean f;
	boolean g;
	boolean e;
	Jamba j;
	
	j = new Jamba();
	f = true;
	g = true;
	e = f && g;
	b = (50);
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
	
	public int yolo(int a) {
		return 3;
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
		return 1;
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
		return 1;
	}
	
	public boolean mufasa() {
		return false;
	}
	
	public int roko() {
		return 4;
	}
}

class Tootsie extends Mokie {
	
	public int jookie(int a, boolean mojo) {
		return a;
	}
	
	public int mufu() {
		return 4;
	}
	
	public int yolo(int a) {
		return 4;
	}
	
}

