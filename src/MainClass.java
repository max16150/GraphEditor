import com.sun.org.apache.bcel.internal.generic.FADD;
import processing.core.PApplet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Locale;

public class MainClass extends PApplet {

    public static void main(String[] args){
        PApplet.main("MainClass", args);
    }

    long startMemoryUsed=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();


    public class Dialog<A, B, C>{
        private Class<A> aType;
        private A aV;
        private Class<B> bType;
        private B bV;
        private Class<C> cType;
        private C cV;

        Dialog(String title, String a, String b, String c, Class<A> tA, Class<B> tB, Class<C> tC){

            this.aType = tA;
            this.bType = tB;
            this.cType = tC;

            JTextField xField = new JTextField(4);
            JTextField yField = new JTextField(4);
            JTextField zField = new JTextField(4);

            int ok = 0;


            while (ok == 0 || ok == 2) {



                JPanel panel = new JPanel(new BorderLayout(10,10));

                JPanel text = new JPanel(new GridLayout(0,1,2,2));

                text.add(new JLabel(a, SwingConstants.RIGHT));
                text.add(new JLabel(b, SwingConstants.RIGHT));
                text.add(new JLabel(c, SwingConstants.RIGHT));


                JPanel field = new JPanel(new GridLayout(0,1,2,2));
                field.add(xField);
                field.add(yField);
                field.add(zField);

                if (ok == 2) {
                    JLabel error_message = new JLabel("Veuillez renseigner les champs avec des valeurs numériques !");
                    error_message.setForeground(Color.RED);

                    panel.add(error_message, BorderLayout.NORTH);
                }



                panel.add(text, BorderLayout.WEST);
                panel.add(field, BorderLayout.CENTER);


                int result = JOptionPane.showConfirmDialog(
                        frame, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        if (aType.isAssignableFrom(Integer.class)) {
                            aV = (A) Integer.valueOf(xField.getText());
                        } else if(aType.isAssignableFrom(String.class)) {
                            aV = (A) xField.getText();
                        } else if(cType.isAssignableFrom(Float.class)) {
                            aV = (A) Float.valueOf(xField.getText());
                        } else {
                            ok = 2;
                        }

                        if (bType.isAssignableFrom(Integer.class)) {
                            bV = (B) Integer.valueOf(yField.getText());
                        } else if(bType.isAssignableFrom(String.class)) {
                            bV = (B) yField.getText();
                        } else if(cType.isAssignableFrom(Float.class)) {
                            bV = (B) Float.valueOf(yField.getText());
                        } else {
                            ok = 2;
                        }

                        if (cType.isAssignableFrom(Integer.class)) {
                            cV = (C) Integer.valueOf(zField.getText());
                        } else if(cType.isAssignableFrom(String.class)) {
                            cV = (C) zField.getText();
                        } else if(cType.isAssignableFrom(Float.class)) {
                            cV = (C) Float.valueOf(zField.getText());
                        } else {
                            ok = 2;
                        }

                        println(xField.getText() + "	" + yField.getText() + "	" + zField.getText());
                        println(aV + "	" + bV + "	" + cV);

                        ok = 1;
                    } catch(Exception e) {
                        ok = 2;
                    }
                } else {
                    ok = 1;
                }
            }

        }

        public A getA(){
            return this.aV;
        }

        public B getB(){
            return this.bV;
        }

        public C getC(){
            return this.cV;
        }
    }


    public static class Liste<T>{
        private T value;
        private Liste<T> nextList;

        public Liste(){

        }

        private Liste(T value){
            this.value = value;
        }

        private Liste<T> getNext(){
            return this.nextList;
        }

        private Liste<T> getLast(){
            Liste<T> occurence = this;
            for (int i = 0; occurence.getNext() != null; i++) {
                occurence = occurence.getNext();
            }
            return occurence;
        }

        private Liste<T> getOccurency(int index){
            Liste<T> occurence = this;
            for (int i = 0; i<index ; i++) {
                occurence = occurence.getNext();
            }
            return occurence;
        }

        public void add(T value){
            Liste<T> instance = this;
            if (instance.value == null) {
                instance.value = value;
            } else {
                instance = instance.getLast();
                instance.nextList = new Liste(value);
            }
        }

        public void remove(int index){
            if (index == 0) {
                this.value = this.getNext().value;
                this.nextList = this.getNext().nextList;
                return;
            }
            Liste<T> occurencyBefore = this.getOccurency(index-1);
            Liste<T> occurencyAfter = this.getOccurency(index+1);
            occurencyBefore.nextList = occurencyAfter;
        }

        public void put(T value, int index){
            Liste<T> toPutList = new Liste(value);
            if (index == 0) {
                toPutList.nextList = this;
                this.value = toPutList.value;
                this.nextList = toPutList.nextList;
                return;
            }
            Liste<T> occurencyBefore = this.getOccurency(index-1);
            Liste<T> occurencyAfter = this.getOccurency(index+1);
            occurencyBefore.nextList = occurencyAfter;
        }

        public int size(){
            int i = 0;
            Liste<T> occurence = this;
            for (i = 0; occurence != null && occurence.value != null; i++) {
                occurence = occurence.getNext();
            }
            return i;
        }

        public void show(){
            Liste<T> occurence = this;
            for (int i = 0; i<this.size(); i++) {
                System.out.println(occurence.value);
                occurence = occurence.getNext();
            }
        }

        public T get(int index){
            Liste<T> occurence = this;
            T value;
            for (int i = 0; i<index; i++) {
                occurence = occurence.getNext();
                if (i+1 == index) {
                    return occurence.value;
                }
            }
            return occurence.value;
        }
    }


    public class Arete{
        private int sommet_initial;
        private int sommet_final;
        private float cout;

        public Arete(int i, int f, float c){
            this.sommet_initial = i;
            this.sommet_final = f;
            this.cout = c;
        }

        public int getInitial(){
            return this.sommet_initial;
        }

        public int getFinale(){
            return this.sommet_final;
        }

        public float getCout(){
            return this.cout;
        }

        public void setInitial(int i){
            this.sommet_initial = i;
        }

        public void setFinale(int f){
            this.sommet_final = f;
        }

        public void setCout(float c){
            this.cout = c;
        }

        public void draw(Graphe g){
            fill(255);
            stroke(255);
            Sommet sommetinitial = g.getSommet(this.sommet_initial);
            Sommet sommetfinal = g.getSommet(this.sommet_final);

            if (this.sommet_initial != this.sommet_final) {
                int ecart = 15;

                int xA = sommetinitial.getX();
                int xB = sommetfinal.getX();

                int yA = sommetinitial.getY();
                int yB = sommetfinal.getY();

                float distAB = dist(xA, yA, xB, yB);

                int x1 = (int)(xA - (ecart*(xA-xB)/distAB));
                int y1 = (int)(yA + (ecart*(yB-yA)/distAB));

                int x2 = (int)(xA - ((distAB-ecart)*(xA-xB)/distAB));
                int y2 = (int)(yA + ((distAB-ecart)*(yB-yA)/distAB));

                this.arrowHead(x1, y1, x2, y2);
                line(x1, y1, x2, y2);
            } else {
                noFill();
                arc(sommetinitial.getX() + 20, sommetinitial.getY(), 40, 40, PI+QUARTER_PI, TWO_PI+3*QUARTER_PI);
                noStroke();
                fill(255);
                this.arrowHead(sommetinitial.getX() + 5, sommetinitial.getY() + 15, sommetinitial.getX()+ 1, sommetinitial.getY() + 7);
            }
            noStroke();
        }

        public void arrowHead(int xA, int yA, int xB, int yB) {
            float size = 4;
            pushMatrix();
            translate(xB, yB);
            rotate(atan2(yB - yA, xB - xA));
            triangle(-size * 2, -size, 0, 0, -size * 2, size);
            popMatrix();
        }

    }


    public class Sommet{
        private int index;
        private int posX;
        private int posY;
        private String name;

        public Sommet(int index){
            this(width/2, height/2, index, "");
        }

        public Sommet(int x, int y, int index){
            this(x, y, index, "");
        }

        public Sommet(int x, int y, int index, String str){
            this.posX = x;
            this.posY = y;
            this.index = index;
            this.name = str;
        }

        public int getX(){
            return this.posX;
        }

        public int getY(){
            return this.posY;
        }

        public void draw(){
            this.afficherSommets();

            if ((mousePressed && (mouseButton == RIGHT || index_sommet_hover != -1) && mouseButton != LEFT && mouseButton != CENTER)
                    && dist(mouseX, mouseY, this.posX, this.posY) < 10
                    && !in_search_of_top_to_dock){

                in_search_of_top_to_dock = true;
                index_first_sommet_to_dock_if_released = this.index;

            } else if (index_second_sommet_to_dock_if_released == this.index && dist(mouseX, mouseY, this.posX, this.posY) > 10){
                index_second_sommet_to_dock_if_released = -1;
            } else if (in_search_of_top_to_dock && dist(mouseX, mouseY, this.posX, this.posY) < 10){
                index_second_sommet_to_dock_if_released = this.index;
            }

        }

        private void afficherSommets(){
            fill(255);
            if (dist(mouseX, mouseY, this.posX, this.posY) < 10 && index_sommet_holded == -1) {
                if (index_sommet_hover == -1 || index_sommet_hover == this.index) {

                    index_sommet_hover = this.index;
                    frame.setCursor(hand_corsor);
                    fill(0,255,0);
                    if (mousePressed && mouseButton == LEFT) {
                        index_sommet_holded = this.index;
                    }
                }
            } else if (index_sommet_hover == this.index && dist(mouseX, mouseY, this.posX, this.posY) > 10) {
                index_sommet_hover = -1;
                frame.setCursor(normal_cursor);
            }

            if (index_sommet_holded == this.index) {

                int margin = 20;

                frame.setCursor(move_cursor);
                fill(255,0,0);
                if (mouseX > margin && mouseY > margin && mouseX < width - margin && mouseY < height - margin - 20) {
                    this.posX = mouseX;
                    this.posY = mouseY;
                }
            }

            String msg = Integer.toString(this.index);

            if (!this.name.equals("")) {
                msg = msg + " - " + this.name;
            }

            text(msg, this.posX, this.posY - 8);
            ellipse(this.posX, this.posY, 5, 5);
        }
    }


    public class Graphe{
        private String name;
        private Liste<Arete> aretes;
        private Liste<Sommet> sommets;


        public Graphe(){
            this.name = "Nouveau Graphe";
            this.aretes = new Liste<Arete>();
            this.sommets = new Liste<Sommet>();
        }

        public String getName(){
            return this.name;
        }

        public Sommet getSommet(int i){
            return this.sommets.get(i-1);
        }

        public void addSommet(){
            Sommet nouveau_sommet = new Sommet((int)random(100,width-100), (int)random(100,height-100), sommets.size()+1);
            this.sommets.add(nouveau_sommet);
        }

        public void addArete(int a, int b, float cout){
            Arete nouvelle_arete = new Arete(a, b, cout);
            this.aretes.add(nouvelle_arete);
        }

        public void draw(){
            // on règle un petit problème rapidement
            if (index_sommet_hover == -1 && index_sommet_holded == -1 && index_onglet_hover == -1) {
                frame.setCursor(normal_cursor);
            }

            // on affiche les sommets
            for (int i =0; i<sommets.size(); i++) {
                sommets.get(i).draw();
            }
            // on affiche les aretes
            for (int i =0; i<aretes.size(); i++) {
                aretes.get(i).draw(this);
            }
        }
    }


    Frame box;
    JFrame frame;
    UIManager ui_manager;
    GrapheManager graphe_manager;

    Cursor hand_corsor = new Cursor(Cursor.HAND_CURSOR);
    Cursor normal_cursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor move_cursor = new Cursor(Cursor.MOVE_CURSOR);

    long curentMemoryUsed;

    int index_sommet_holded = -1;
    int index_sommet_hover = -1;
    int index_second_sommet_to_dock_if_released = -1;
    int index_first_sommet_to_dock_if_released = -1;
    boolean in_search_of_top_to_dock = false;
    int index_onglet_hover = -1;

    NumberFormat nf = NumberFormat.getInstance(new Locale("fr", "FR"));


    public void mouseReleased(){
        index_sommet_holded = -1;
        index_second_sommet_to_dock_if_released = -1;
        in_search_of_top_to_dock = false;
    }


    public void setup(){
        surface.setSize(900,400);
        surface.setResizable(true);
        frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas)this.getSurface().getNative()).getFrame();
        frame.setTitle("Graphe Controller");

        nf.setMaximumFractionDigits(2);

        box = new Frame();

        ui_manager = new UIManager();

        graphe_manager = new GrapheManager();
        //Graphe graphe = saisie_graphe();
        //afficher_matrice(adjacence(graphe));

    }


    public void draw(){

        if (mouseButton == LEFT){
            in_search_of_top_to_dock = false;
            index_second_sommet_to_dock_if_released = -1;
            index_first_sommet_to_dock_if_released = -1;
        }

        curentMemoryUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();

        textSize(12);
        background(40,41,35);

        fill(255);
        textAlign(RIGHT);
        text("Réalisé par Gabriel Blanchot et Maxime Boissout", width - 5, height - 30);

        ui_manager.draw();
        graphe_manager.drawCurentGraphe();

        fill(255);
        textAlign(LEFT);
        text(nf.format(frameRate) + " fps", 20, 50);
        text(nf.format(((curentMemoryUsed-startMemoryUsed)/1000000.0)) + " Mo", 20,70);
        text(nf.format(((curentMemoryUsed/1000000.0))) + " Mo", 20,90);
    }


    public class GrapheManager{
        private Liste<Graphe> graphes;
        private int graphe_amount;
        private int curent_graphe;

        GrapheManager(){
            this.graphes = new Liste<Graphe>();
            this.graphe_amount = 0;
            this.curent_graphe = 0;
        }

        //Créé un nouveau graphe et le met en onglet principal
        public void newGraphe(){
            this.graphes.add(new Graphe());
            curent_graphe = graphe_amount + 1;
            graphe_amount ++;
        }

        public void setCurentGraphe(int i){
            this.curent_graphe = i;
        }

        public Graphe getGraphe(int i){
            return this.graphes.get(i);
        }

        public int getAmountOfGraphe(){
            return this.graphe_amount;
        }

        public int getNumberOfCurentGrahe(){
            return this.curent_graphe;
        }

        public Graphe getCurentGraphe(){
            if (graphe_amount <= 0) {
                return null;
            }
            return graphes.get(curent_graphe-1);
        }

        public void removeCurentGraphe(){
            this.graphes.remove(this.curent_graphe);
        }

        public void removeGraphe(int i){
            this.graphes.remove(i);
        }

        public void drawCurentGraphe(){
            if (!(this.graphes.size() <= 0)) {
                this.getCurentGraphe().draw();
            }
        }
    }


    public class UIManager implements MouseListener {

        public UIManager(){

        }

        public void draw(){
            afficherOnglets();
            afficherAretesEnCourDeCreation();
        }

        private void afficherOnglets(){
            int nombre_de_graphe = graphe_manager.getAmountOfGraphe();

            if (nombre_de_graphe <= 0) {
                box.grapheClickabke(false);
            } else {
                box.grapheClickabke(true);
            }

            int largeur_onglet = 150;

            noStroke();
            rectMode(CORNER);
            fill(24, 25, 21);
            rect(0, 0, width, 35);

            for (int i = 0; i < nombre_de_graphe; i++) {

                if (graphe_manager.getNumberOfCurentGrahe()-1 == i) {
                    fill(40, 41, 35);
                } else {
                    fill(32, 33, 28);
                }

                rect(10 + i*largeur_onglet, 5, largeur_onglet, 30, 10, 10, 0, 0);
                textAlign(LEFT);
                fill(255);
                textSize(12);
                text(graphe_manager.getGraphe(i).getName(), 10 + i*largeur_onglet + 10, 25);

                if (mouseY < 35 && mouseY > 0 && mouseX > 10 + i*largeur_onglet && mouseX < 10 + (i+1)*largeur_onglet) {
                    index_onglet_hover = 1;
                    frame.setCursor(hand_corsor);
                    if (mousePressed) {
                        graphe_manager.setCurentGraphe(i+1);
                    }
                }
            }

            if (!(mouseY < 35 && mouseY > 0 && mouseX > 10 + 0*largeur_onglet && mouseX < 10 + (graphe_manager.getAmountOfGraphe())*largeur_onglet)) {
                index_onglet_hover = -1;
            }

            textAlign(CENTER);
        }

        private void afficherAretesEnCourDeCreation(){
            if (mousePressed && mouseButton == RIGHT && in_search_of_top_to_dock){

                fill(255);
                stroke(255);
                int ecart = 15;

                int xA = graphe_manager.getCurentGraphe().getSommet(index_first_sommet_to_dock_if_released).getX();
                int xB = mouseX;

                int yA = graphe_manager.getCurentGraphe().getSommet(index_first_sommet_to_dock_if_released).getY();
                int yB = mouseY;

                if (index_second_sommet_to_dock_if_released != -1){
                    xB = graphe_manager.getCurentGraphe().getSommet(index_second_sommet_to_dock_if_released).getX();
                    yB = graphe_manager.getCurentGraphe().getSommet(index_second_sommet_to_dock_if_released).getY();
                }

                System.out.println("X1:" + xA + " Y1:" + yA +" X2:" + xB + " Y2:" + yB);

                float distAB = dist(xA, yA, xB, yB);

                int x1 = (int)(xA - (ecart*(xA-xB)/distAB));
                int y1 = (int)(yA + (ecart*(yB-yA)/distAB));

                int x2 = (int)(xA - ((distAB-ecart)*(xA-xB)/distAB));
                int y2 = (int)(yA + ((distAB-ecart)*(yB-yA)/distAB));

                arrowHead(x1, y1, x2, y2);
                line(x1, y1, x2, y2);
            }
        }

        public void arrowHead(int xA, int yA, int xB, int yB) {
            float size = 4;
            pushMatrix();
            translate(xB, yB);
            rotate(atan2(yB - yA, xB - xA));
            triangle(- size * 2 , - size, 0, 0, - size * 2, size);
            popMatrix();
        }


        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }


    public class Frame extends JFrame implements ActionListener {

        private JMenuItem action_nouveau_graphe;
        private JMenuItem action_ouvrir_un_graphe;
        private JMenuItem action_fermer_ce_graphe;
        private JMenuItem action_fermer_tout_les_graphes;
        private JMenuItem action_enregistrer_sous;
        private JMenuItem action_imprimer;
        private JMenuItem action_quitter;

        private JMenuItem action_revenir_en_arriere;

        private JMenuItem action_calcul_matrice_adjacente;
        private JMenuItem action_calcul_matrice_transitive;

        private JMenuItem action_ajouter_une_arete;
        private JMenuItem action_ajouter_un_sommet;
        private JMenuItem action_supprimer_une_arete;
        private JMenuItem action_supprimer_un_sommet;
        private JMenuItem action_modifier_une_arete;

        private JMenuItem action_theme;

        private JMenuItem action_mise_a_jour;
        private JMenuItem action_documentation;

        private JPanel panel;

        public Frame() {

            frame.setLocationRelativeTo(null);
            JMenuBar barre = new JMenuBar();

            frame.setJMenuBar(barre);


            JMenu fichier_menu = new JMenu("Fichier");
            JMenu edition_menu = new JMenu("Édition");
            JMenu outils_menu = new JMenu("Outils");
            JMenu graphe_menu = new JMenu("Graphe");
            JMenu preference_menu = new JMenu("Préférences");
            JMenu aide_menu = new JMenu("Aide");


            barre.add(fichier_menu);
            barre.add(edition_menu);
            barre.add(outils_menu);
            barre.add(graphe_menu);
            barre.add(preference_menu);
            barre.add(aide_menu);

            // --------------------------------------------------------------

            action_nouveau_graphe = new JMenuItem("Nouveau Graphe");
            action_ouvrir_un_graphe = new JMenuItem("Ouvrir un Graphe");
            action_fermer_ce_graphe = new JMenuItem("Fermer ce Graphe");
            action_fermer_tout_les_graphes = new JMenuItem("Fermer tout les Graphes");
            action_enregistrer_sous = new JMenuItem("Enregistrer sous");
            action_imprimer = new JMenuItem("Imprimer ce Graphe");
            action_quitter = new JMenuItem("Quitter");

            action_nouveau_graphe.addActionListener(this);
            action_ouvrir_un_graphe.addActionListener(this);
            action_fermer_ce_graphe.addActionListener(this);
            action_fermer_tout_les_graphes.addActionListener(this);
            action_enregistrer_sous.addActionListener(this);
            action_imprimer.addActionListener(this);
            action_quitter.addActionListener(this);

            fichier_menu.add(action_nouveau_graphe);
            fichier_menu.add(action_ouvrir_un_graphe);
            fichier_menu.addSeparator();
            fichier_menu.add(action_fermer_ce_graphe);
            fichier_menu.add(action_fermer_tout_les_graphes);
            fichier_menu.addSeparator();
            fichier_menu.add(action_enregistrer_sous);
            fichier_menu.addSeparator();
            fichier_menu.add(action_imprimer);
            fichier_menu.addSeparator();
            fichier_menu.add(action_quitter);

            // --------------------------------------------------------------

            action_revenir_en_arriere = new JMenuItem("Revenir en arrière");

            action_revenir_en_arriere.addActionListener(this);

            edition_menu.add(action_revenir_en_arriere);

            // --------------------------------------------------------------

            JMenu calculer_menu = new JMenu("Calculer");
            action_calcul_matrice_adjacente = new JMenuItem("Matrice Adjacente");
            action_calcul_matrice_transitive = new JMenuItem("Matrice Transitive");

            action_calcul_matrice_adjacente.addActionListener(this);
            action_calcul_matrice_transitive.addActionListener(this);

            calculer_menu.add(action_calcul_matrice_adjacente);
            calculer_menu.add(action_calcul_matrice_transitive);
            outils_menu.add(calculer_menu);

            // --------------------------------------------------------------

            action_ajouter_un_sommet = new JMenuItem("Ajouter un Sommet");
            action_ajouter_une_arete = new JMenuItem("Ajouter une Arête");
            action_modifier_une_arete = new JMenuItem("Modifier une Arête");
            action_supprimer_un_sommet = new JMenuItem("Supprimer un sommet");
            action_supprimer_une_arete = new JMenuItem("Supprimer une Arête");

            action_ajouter_un_sommet.addActionListener(this);
            action_ajouter_une_arete.addActionListener(this);
            action_modifier_une_arete.addActionListener(this);
            action_supprimer_un_sommet.addActionListener(this);
            action_supprimer_une_arete.addActionListener(this);

            graphe_menu.add(action_ajouter_un_sommet);
            graphe_menu.add(action_ajouter_une_arete);
            graphe_menu.addSeparator();
            graphe_menu.add(action_modifier_une_arete);
            graphe_menu.addSeparator();
            graphe_menu.add(action_supprimer_un_sommet);
            graphe_menu.add(action_supprimer_une_arete);

            // --------------------------------------------------------------

            action_theme = new JMenuItem("Theme");

            action_theme.addActionListener(this);

            preference_menu.add(action_theme);

            // --------------------------------------------------------------

            action_mise_a_jour = new JMenuItem("Mise à jour");
            action_documentation = new JMenuItem("Documentation");

            action_mise_a_jour.addActionListener(this);
            action_documentation.addActionListener(this);

            aide_menu.add(action_mise_a_jour);
            aide_menu.addSeparator();
            aide_menu.add(action_documentation);

            // --------------------------------------------------------------


            frame.setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {

            Object src = e.getSource();

            if (src == action_ajouter_un_sommet) {
                println("Ajouter un nouveau sommet");
                graphe_manager.getCurentGraphe().addSommet();


            } else if (src == action_ajouter_une_arete) {
                System.out.println("Ajouter une nouvelle arête");

                Dialog dial = new Dialog<Integer, Integer, Float>(
                        "Nouvelle arête",
                        "Sommet de départ de l'arête :",
                        "Sommet de fin de l'arête :",
                        "Coût de l'arête :",
                        Integer.class,
                        Integer.class,
                        Float.class);

                if (dial.getA() != null && dial.getB() != null && dial.getC() != null) {
                    int a = (int)dial.getA();
                    int b = (int)dial.getB();
                    float c = (float)dial.getC();

                    graphe_manager.getCurentGraphe().addArete(a, b, c);
                }


            } else if (src == action_supprimer_une_arete) {
                System.out.println("Supprimer une arête");


            } else if (src == action_supprimer_un_sommet) {
                System.out.println("Supprimer un sommet");


            } else if (src == action_calcul_matrice_transitive) {
                System.out.println("Calcul de la matrice transitive");


            } else if (src == action_calcul_matrice_adjacente) {
                System.out.println("Calcul de la matrice adjacente");


            } else if (src == action_quitter) {
                exit();
            } else if (src == action_nouveau_graphe) {
                graphe_manager.newGraphe();
            }

        }

        public void grapheClickabke(boolean b){
            action_ajouter_une_arete.setEnabled(b);
            action_ajouter_un_sommet.setEnabled(b);
            action_supprimer_une_arete.setEnabled(b);
            action_supprimer_un_sommet.setEnabled(b);
            action_modifier_une_arete.setEnabled(b);
        }
    }
}
