package graphics;

public class Camera {

  public void orbit(double dx, double dy) {
    System.out.printf("dx=%5.3f dy=%5.3f \n", dx, dy);
  }
  
  public void dolly(double dy) {
    System.out.printf("dy=%f \n", dy);
  }
  
}

