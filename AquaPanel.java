import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class AquaPanel extends JPanel implements ActionListener
{
   private static final long serialVersionUID = 1L;
   private final String BACKGROUND_PATH = "aquarium_background.jpg";
   //private final String BACKGROUND_PATH = "D:\\Class_files\\aquarium_background.jpg";
   private AquaFrame frame;
   private boolean isFood;
   private JPanel p1;
   private JButton[] b_num;
   private String[] names = {"Add Animal","Sleep","Wake up","Reset","Food","Info","Exit"};
   public Set<Swimmable> animals;
   private JScrollPane scrollPane;
   private boolean isTableVisible;
   private int totalCount;
   private BufferedImage img;
   private boolean bgr;
   private CyclicBarrier barrier;
   
   public AquaPanel(AquaFrame f)
   {
	    frame = f;
	    isFood = false;
	    totalCount = 0;
	    barrier = null;
	    isTableVisible = false;
	    animals = new HashSet<Swimmable>();
	    
	    setBackground(new Color(255,255,255));
	   
	    p1=new JPanel();
		p1.setLayout(new GridLayout(1,7,0,0));
		p1.setBackground(new Color(0,150,255));

		b_num=new JButton[names.length];
		for(int i=0;i<names.length;i++)
		{
		    b_num[i]=new JButton(names[i]);
		    b_num[i].addActionListener(this);
		    b_num[i].setBackground(Color.lightGray);
		    p1.add(b_num[i]);		
		}

		setLayout(new BorderLayout());
		add("South", p1);
		
		img = null;
		bgr = false;
		try { img = ImageIO.read(new File(BACKGROUND_PATH)); } 
		catch (IOException e) { System.out.println("Cannot load image"); }

		
   }		

   public void paintComponent(Graphics g)
   {
	   	super.paintComponent(g);	
	   	
	   	if(bgr && (img!=null))
	   	{
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
	   	}

	   	if(isFood)
	   	{
	   		Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.red);
            g2.drawArc(getWidth()/2, getHeight()/2-5, 10, 10, 30, 210);	   		
            g2.drawArc(getWidth()/2, getHeight()/2+5, 10, 10, 180, 270);	   		
            g2.setStroke(new BasicStroke(1));
	   	}
	    
	   	for(Swimmable sw : animals)
	   		sw.drawAnimal(g);
   }
   
   public void setBackgr(int num) {
	   switch(num) {
	   case 0:
		   setBackground(new Color(255,255,255));
		   bgr = false; 
		   break;
	   case 1:
		   setBackground(new Color(0,150,255));
		   bgr = false; 
		   break;
	   default:
			bgr = true;   
	   }
	   repaint();
   }
   
   synchronized public boolean checkFood()
   {
	   return isFood;
   }

   /**
    * CallBack function 
    * @param f
    */
   synchronized public void eatFood(Swimmable sw)
   {
	   if(isFood)
	   {
		   	isFood = false;
	   		sw.eatInc();
	   		totalCount++;
	   		System.out.println("The "+sw.getColor()+" "+sw.getAnimalName()+" with size "+sw.getSize()+" ate food.");
	   }
	   else
	   {
		   System.out.println("The "+sw.getColor()+" "+sw.getAnimalName()+" with size "+sw.getSize()+" missed food.");
	   }
   }

   public void add()
   {
	   if(animals.size()==5) {
		   JOptionPane.showMessageDialog(frame, "You cannot add more than 5 animals");
	   }
	   else {
		   AddAnimalDialog dial = new AddAnimalDialog(frame,this,"Add an animal to aquarium");
		   dial.setVisible(true);
	   }
   }
   
   public void addAnimal(String animal, int sz, int hor, int ver, Color c)
   {
	   Swimmable sw;
	   if(animal.equals("Fish"))
		   sw = new Fish(sz,0,0,hor,ver,c,this);
	   else
		   sw = new Jellyfish(sz,0,0,hor,ver,c,this);
	   sw.start();
	   animals.add(sw);
   }

	public void start() {
	   for(Swimmable sw : animals)
		   sw.setResume();
   }

 	public void stop() {
	   for(Iterator<Swimmable> i = animals.iterator(); i.hasNext(); )
			i.next().setSuspend();
   }

   synchronized public void reset()
   {
	   animals.clear();
	   isFood = false;
	   totalCount = 0;
	   repaint();
   }

   synchronized public void food()
   {
	   if(!animals.isEmpty())
	   {
		   barrier = new CyclicBarrier(animals.size());
		   for(Swimmable sw : animals)
			   sw.setBarrier(barrier);
	   }
	   isFood = true;
	   repaint();
   }
   
   public void info()
   {  	 
	   if(isTableVisible == false)
	   {
		  int i=0;
		  String[] columnNames = {"Animal","Color","Size","Hor. speed","Ver. speed","Eat counter"};
	      String [][] data = new String[animals.size()+1][columnNames.length];
	      for(Swimmable sw : animals)
	      {
	    	  data[i][0] = sw.getAnimalName();
	    	  data[i][1] = sw.getColor();
	    	  data[i][2] = String.valueOf(sw.getSize());
	    	  data[i][3] = String.valueOf(sw.getHorSpeed());
	    	  data[i][4] = String.valueOf(sw.getVerSpeed());
	    	  data[i][5] = String.valueOf(sw.getEatCount());
	    	  i++;
	      }
	      data[i][0] = "Total";
	      data[i][5] = String.valueOf(totalCount);
	      
	      JTable table = new JTable(data, columnNames);
	      scrollPane = new JScrollPane(table);
	      scrollPane.setSize(450,table.getRowHeight()*(animals.size()+1)+24);
	      add( scrollPane, BorderLayout.CENTER );
	      isTableVisible = true;
	   }
	   else
	   {
		   isTableVisible = false;
	   }
	   scrollPane.setVisible(isTableVisible);
       repaint();
   }

   public void destroy()
   {  	        
      System.exit(0);
   }
   
   public void actionPerformed(ActionEvent e)
   {
	if(e.getSource() == b_num[0]) // "Add Animal"
		add();
	else if(e.getSource() == b_num[1]) // "Sleep"
		stop();
	else if(e.getSource() == b_num[2]) // "Wake up"
		start();
	else if(e.getSource() == b_num[3]) // "Reset"
		reset();
	else if(e.getSource() == b_num[4]) // "Food"
		food();
	else if(e.getSource() == b_num[5]) // "Info"
		info();
	else if(e.getSource() == b_num[6]) // "Exit"
		destroy();
   }

}