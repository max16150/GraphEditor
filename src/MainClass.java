/*
* Certaines variables, méthodes, et fonctions ont étés extraites des classes internes de processing qui ne sont pas dans la documentation.
* Nous avons utilisé IntelliJ IDEA pour coder ce programme, il nous a permis de comprendre la mécanique interne de processing grace a ses outils
* de recherche.
*
*
* */



import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;
import processing.core.PApplet;
import processing.event.MouseEvent;
import sun.java2d.SunGraphics2D;

import javax.lang.model.type.NullType;
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
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.lang.Integer;

public class MainClass extends PApplet {

    public static void main(String[] args){
        PApplet.main("MainClass", args);
    }

    /* Utilisé pour calculer la mémoire utilisée par mon programme */
    long startMemoryUsed=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();

    /* Classe me permettant d'ouvrir une fenetre de dialogue avec l'utilisateur
    * partiellement modulable en fonction de mes besoins dans le code
    * Elle sert notemment pour demander les valeurs d'une nouvelle Arrete ou demander simplement le nom d'un nouveau graphe
    * Je peux choisir le titre de la fenetre
    * mais aussi les questions
    * et le type de valeur que me renverra la fenetre de dialogue une fois fermée
    * si le type de valeur que l'utilisateur rentre n'est pas bon, elle se réouvre et affiche un message
    * A, B et C correspondent aux types de valeurs que veux que l'utilisateur rentre (Integer, Float, String ...)*/
    public class Dialog<A, B, C>{
        /* Le comportement de ma classe change en fonction du Type de valeurs que je veux qu'elle me renvoie
        * J'ai donc utilisé la Reflexion de Java me permettant de modifier le comportement de mon objet durant l'exécution
        * aType recevra Integer.class dans le cas ou je voudrais que la première valeur que rentre l'utilisateur
        * soit un entier
        *
        * aV bV et cV prendront les valeurs que l'utilisateur rentrera dans les champs de texte
        * j'utilise la généricité pour pouvoir la encore décider du type de valeur
        * aType et aV sont étroitement liées,
        * si je veux que l'utilisateur rentre un entier dans un champs A = Integer et aType = Integer.class*/
        private Class<A> aType;
        private A aV;
        private Class<B> bType;
        private B bV;
        private Class<C> cType;
        private C cV;

        /* Unique constructeur de la fenetre de dialogue, elle peut demander jusqu'a 3 valeurs en meme temps car je
        * n'ai jamais besoin d'en demander plus*/
        Dialog(String title, String a, String b, String c, Class<A> tA, Class<B> tB, Class<C> tC){

            this.aType = tA;
            this.bType = tB;
            this.cType = tC;

            /* Création des zones de texte */
            JTextField xField = new JTextField(4);
            JTextField yField = new JTextField(4);
            JTextField zField = new JTextField(4);


            /* ok me permet de savoir si l'utilisateur a rentré les données que je lui demande dans le bon type*/
            int ok = 0;

            /* Tourne autant de fois qu'il faudra pour que l'utilisateur rentre les données dans le bon type*/
            while (ok == 0 || ok == 2) {

                // Défini un endroit ou je pourrais afficher mes champs de texte et le texte de mes question
                JPanel panel = new JPanel(new BorderLayout(10,10));

                JPanel text = new JPanel(new GridLayout(0,1,2,2));

                JPanel field = new JPanel(new GridLayout(0,1,2,2));

                if (!aType.isAssignableFrom(NullType.class)) {
                    text.add(new JLabel(a, SwingConstants.RIGHT));
                    field.add(xField);
                }

                if (!bType.isAssignableFrom(NullType.class)) {
                    text.add(new JLabel(b, SwingConstants.RIGHT));
                    field.add(yField);
                }

                if (!cType.isAssignableFrom(NullType.class)) {
                    text.add(new JLabel(c, SwingConstants.RIGHT));
                    field.add(zField);
                }

                // Si l'utilisateur a mal rempli les champs de texte, un message d'erreur s'affiche sur la fenetre
                if (ok == 2) {
                    JLabel error_message = new JLabel("Veuillez renseigner les champs avec des valeurs numériques !");
                    error_message.setForeground(Color.RED);

                    panel.add(error_message, BorderLayout.NORTH);
                }



                panel.add(text, BorderLayout.WEST);
                panel.add(field, BorderLayout.CENTER);

                /* Quand ma fenetre vas s'ouvrir, je n'aurais plus aucun moyen d'intéragir dessus avec le code puisque
                * l'avancé du code vas s'arreter a la ligne, en attendant que l'utilisateur valide ou non pour continuer de s'exécuter.
                * Je veux que le curseur sois dirrectement sur le premier champs de texte de ma fenetre de dialogue
                * de ce fait j'utilise l'AncestorListener, qui vas réagir quand xField (qui est le champs de texte)
                * vas apparaitre a l'écran et a ce moment la, il vas instancier un nouveau FocusRequest (qui est une classe codée
                * plus loin dans le code) et qui vas me permettre de mettre le curseur sur mon champs de texte*/
                xField.addAncestorListener(new FocusRequest(false));

                /* Affichage de la fenetre de dialogue, cette méthode est bloquante, tant que
                * l'utilisateur ne cliquera pas sur OK ou annuler, le code attendra ici */
                int result = JOptionPane.showConfirmDialog(
                        frame,
                        panel,
                        title,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                /* Une fois la fenetre fermée, j'enlève le listener, pour pouvoir le refaire si jamais le code doit réouvrir
                * une nouvelle fenetre*/
                xField.addAncestorListener(new FocusRequest(true));

                /* Si l'utilisateur clique sur OK je vérifie que ce qu'il a rentré concorde avec les types que mon code a besoin*/
                if (result == JOptionPane.OK_OPTION) {
                    try {

                        if (aType.isAssignableFrom(Integer.class)) {
                            /* ici par exemple, dans le cas ou aType = Integer.class, j'essaye de convertir la valeur
                            * du premier champs de texte en un entier, si ça ne fonctionne pas, ceci vas générer une erreur
                            * et grace au block catch{} je vais pouvoir changer la valeur de ok en 2 */
                            aV = (A) Integer.valueOf(xField.getText());
                        } else if(aType.isAssignableFrom(String.class)) {
                            aV = (A) xField.getText();
                        } else if(aType.isAssignableFrom(Float.class)) {
                            aV = (A) Float.valueOf(xField.getText());
                        } else {
                            ok = 2;
                        }

                        if (bType.isAssignableFrom(Integer.class)) {
                            bV = (B) Integer.valueOf(yField.getText());
                        } else if(bType.isAssignableFrom(String.class)) {
                            bV = (B) yField.getText();
                        } else if(bType.isAssignableFrom(Float.class)) {
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
                    /* Si jamais l'utilisateur clique sur Annuler, ok prend la valeur 1 et me permet de ne pas réouvrir une fenetre*/
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


    /* Ma classe Liste codée en début de semestre, elle est assez complète et me permet beaucoup d'action sur mes listes
    * Je peux faire une liste de nimporte quel Objet grace a la généricité
    * Je ferais par la suite des listes de Sommet Arete et Graphe*/
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


    /* Classe régissant une arete, plusieures méthodes me permettent une optimisation de lecture
    * l'affichage de l'arete est dirrectement implémenté dans cette classe avec la méthode draw()*/
    public class Arete{
        /* Prend en variable de classe le numéro de son sommet de départ ainsi que de fin et une valeur propre a l'instance*/
        private int sommet_initial;
        private int sommet_final;
        private float poid;
        private String libele;

        public Arete(int i, int f, float c){
            this.sommet_initial = i;
            this.sommet_final = f;
            this.poid = c;
        }

        public Arete(int i, int f, String libele){
            this.sommet_initial = i;
            this.sommet_final = f;
            this.libele = libele;
        }

        public Arete(int i, int f){
            this.sommet_initial = i;
            this.sommet_final = f;
        }

        public int getInitial(){
            return this.sommet_initial;
        }

        public int getFinale(){
            return this.sommet_final;
        }

        public float getPoid(){
            return this.poid;
        }

        public String getLibele() {
            return this.libele;
        }

        public void setLibele(String libele) {
            this.libele = libele;
        }

        public void setInitial(int i){
            this.sommet_initial = i;
        }

        public void setFinale(int f){
            this.sommet_final = f;
        }

        public void setPoid(float c){
            this.poid = c;
        }

        public void draw(){

            Graphe g = graphe_manager.getCurentGraphe();

            Sommet sommetinitial = g.getSommet(this.sommet_initial);
            Sommet sommetfinal = g.getSommet(this.sommet_final);

            int type = g.getType();
            String str = "";

            if (type == Graphe.ORIENTE_LIBELE || type == Graphe.NON_ORIENTE_LIBELE){
                str = this.getLibele();
            } else if (type == Graphe.ORIENTE_PONDERE || type == Graphe.NON_ORIENTE_PONDERE){
                str = Float.toString(this.getPoid());
            }

            ui_manager.afficherArete(sommetinitial,sommetfinal,str, g.getType(), this);
        }
    }


    /* Classe définissant les sommets*/
    public class Sommet{
        /* Un sommet a une position bien définie sur la fenetre d'affichage (posX, posY)
        * Il possède un index (c'est son numéro)
        * Et il peux posséder un nom qui s'affichera a l'écran au dessus de lui si il est défini */
        private int index;
        private int posX;
        private int posY;
        private String name;
        int color;

        /* Trois constructeurs pour mes sommets*/
        /* un qui prend simplement un numéro de sommet
         * Dans ce cas il créra un sommet au millieu de l'écran et n'aura pas de nom*/
        public Sommet(int index){
            this(width/2, height/2, index, "");
        }

        /* Un qui prend les coordonées du point et son numéro
         * Dans ce cas il n'aura pas de nom*/
        public Sommet(int x, int y, int index){
            this(x, y, index, "");
        }

        /* dans ce cas il prend toutes les valeurs qu'on lui demande*/
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

        public int getColor() {
            return this.color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        /* Méthode qui gère l'affichage des sommets*/
        public void draw(){
            this.afficherSommets();


            if ((
                    mousePressed && (mouseButton == RIGHT || index_sommet_hover != -1) && mouseButton != LEFT && mouseButton != CENTER)
                    && dist(mouseX, mouseY, this.posX, this.posY) < 10
                    && !in_search_of_top_to_dock){

                /* Dans le cas ou je clique sur un sommet avec le clic gauche, je déplace le sommet*/
                in_search_of_top_to_dock = true;
                index_first_sommet_to_dock_if_released = this.index;

            } else if (
                    in_search_of_top_to_dock
                    && index_second_sommet_to_dock_if_released == this.index
                    && dist(mouseX, mouseY, this.posX, this.posY) > 10){

                /* Dans le cas ou je serais en train de créer une arete avec le clik droit,
                * je vérifi si le sommet final enregistré correspond a l'index de cette instance,
                * je vérifie si ma souris est bien sur ce sommet, dnas le cas ou elle n'y serais plus (distance > 10)
                * alors je remet le sommet final "en jeux"
                * cela évite que le sommet de fin reste bloqué a une instance*/
                index_second_sommet_to_dock_if_released = -1;

            } else if (
                    in_search_of_top_to_dock
                    && dist(mouseX, mouseY, this.posX, this.posY) < 10){

                /* Dans le cas ou je serais en train de créer une arrete avec le clik droit,
                * si jamais ma sourie est a moins de 10 pixels des coordonées de l'instance de ce sommet alors je considère
                * que le sommet que l'utilisateur veux lier est celui ci*/
                index_second_sommet_to_dock_if_released = this.index;

            }

        }

        /* Méthode qui affiche les sommets, si jamais la souris de l'utilisateur est sur l'un de ces sommets, alors le curseur change de forme
        * et le sommet change de couleur; si jamais l'utilisateur clique gauche sur un sommet, il peux le déplacer,
        * le curseur change donc de forme et le sommet change de couleur*/
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
                if (mouseX > margin && mouseY > margin + 30 && mouseX < width - margin && mouseY < height - margin - 40) {
                    this.posX = mouseX;
                    this.posY = mouseY;
                }
            }

            String msg = Integer.toString(this.index);

            if (!this.name.equals("")) {
                msg = msg + " - " + this.name;
            }

            if (in_search_of_top_to_dock){
                stroke(255);
                strokeWeight(3);
            }

            text(msg, this.posX, this.posY - 8);
            ellipse(this.posX, this.posY, 5, 5);

            noStroke();
            strokeWeight(1);
        }
    }


    /* Classe qui régie chaques graphes */
    public class Graphe{

        /* Chaques graphes a un nom, et possède une liste de sommet et d'aretes*/
        private String name;
        private Liste<Arete> aretes;
        private Liste<Sommet> sommets;
        private int type;

        public static final int     ORIENTE             = 1;
        public static final int     ORIENTE_PONDERE     = 2;
        public static final int     ORIENTE_LIBELE      = 3;
        public static final int     NON_ORIENTE         = 4;
        public static final int     NON_ORIENTE_PONDERE = 5;
        public static final int     NON_ORIENTE_LIBELE  = 6;


        /* Chaques graphes créé est nomé par défaut "nouveau graphe"
        * j'instancie les listes de sommets et d'aretes*/
        public Graphe(int i){
            this.name = "Nouveau Graphe";
            this.aretes = new Liste<Arete>();
            this.sommets = new Liste<Sommet>();
            this.type = i;
        }

        public String getName(){
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Sommet getSommet(int i){
            return this.sommets.get(i-1);
        }

        /* rajoute un sommet a la liste des sommets du graphe, il aura une position aléatoire sur la
        * fenetre et portera un numéro équivalent au numéro du dernier sommet +1*/
        public void addSommet(){
            Sommet nouveau_sommet = new Sommet((int)random(100,width-100), (int)random(100,height-100), sommets.size()+1);
            this.sommets.add(nouveau_sommet);
        }

        /* Ajoute une arrete a la liste des aretes du graphe*/
        public void addArete(int a, int b, float poid){
            Arete nouvelle_arete = new Arete(a, b, poid);
            this.aretes.add(nouvelle_arete);
        }

        public void addArete(int a, int b, String libele){
            Arete nouvelle_arete = new Arete(a, b, libele);
            this.aretes.add(nouvelle_arete);
        }

        public void addArete(int a, int b){
            Arete nouvelle_arete = new Arete(a, b);
            this.aretes.add(nouvelle_arete);
        }

        public void addAreteDialog(int a, int b){
            int type = graphe_manager.getCurentGraphe().getType();

            if (type == Graphe.NON_ORIENTE || type == Graphe.ORIENTE){

                this.addArete(a, b);

            } else if (type == Graphe.ORIENTE_PONDERE || type == Graphe.NON_ORIENTE_PONDERE){
                Dialog dial = new Dialog<Float, NullType, NullType>(
                        "Nouvelle arête",
                        "Poid de l'arête :",
                        "",
                        "",
                        Float.class,
                        NullType.class,
                        NullType.class);

                /* On réculère les valeurs que l'utilisateur a rentré*/
                if (dial.getA() != null) {
                    float c = (float)dial.getA();

                    this.addArete(a, b, c);
                }
            } else if (type == Graphe.ORIENTE_LIBELE || type == Graphe.NON_ORIENTE_LIBELE){
                Dialog dial = new Dialog<String, NullType, NullType>(
                        "Nouvelle arête",
                        "Libelé de l'arête :",
                        "",
                        "",
                        String.class,
                        NullType.class,
                        NullType.class);

                /* On réculère les valeurs que l'utilisateur a rentré*/
                if (dial.getA() != null) {
                    String c = (String)dial.getA();

                    this.addArete(a, b, c);
                }
            }
        }

        /* Dessine le graphe en appelant les méthodes draw de tout les sommets et aretes du graphe*/
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
                aretes.get(i).draw();
            }
        }

        public int getAmountOfSommets(){
            return this.sommets.size();
        }

        public int getAmountOfAretes(){
            return this.aretes.size();
        }

        public int getType(){
            return this.type;
        }

        public String getStringType(){
            switch (this.type){
                case 1:
                    return "Graphe Orienté";
                case 2:
                    return "Graphe Orienté Pondéré";
                case 3:
                    return "Graphe Orienté Libelé";
                case 4:
                    return "Graphe Non Orienté";
                case 5:
                    return "Graphe Non Orienté Pondéré";
                case 6:
                    return "Graphe Non Orienté Libelé";
            }
            return "c'est bizare si ça sort ça mdrr";
        }
    }


    public void mouseWheel(MouseEvent event) {
        int e = (int)event.getCount();
    }


    Menu menu;
    JFrame frame;
    UIManager ui_manager;
    GrapheManager graphe_manager;
    Debuger debuger;

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
    int index_onglet_holded = -1;
    Arete arete_hover = null;
    int index_arete_hover = -1;
    int clic_x = -1;
    int clic_y = -1;

    /* Me permet de calculer la mémoire utilisée par mon programme*/
    NumberFormat nf = NumberFormat.getInstance(new Locale("fr", "FR"));


    public void mouseReleased(){
        index_sommet_holded = -1;
        index_onglet_holded = -1;
        clic_y = -1;
        clic_x = -1;
    }

    public void mouseClick(){
        clic_x = mouseX;
        clic_y = mouseY;
    }


    public void setup(){
        surface.setSize(900,400);
        surface.setResizable(true);
        frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas)this.getSurface().getNative()).getFrame();
        frame.setTitle("Graphe Controller");

        nf.setMaximumFractionDigits(2);

        /* Créé une nouvelle instance de UIManager, me permettant de gérer l'interface graphique du programme*/
        ui_manager = new UIManager();

        /* Créé une nouvelle instance de GrapheManager, me permettant de gérer mes graphes*/
        graphe_manager = new GrapheManager();

        debuger = new Debuger();

        //textSize(50);

        //PFont temp = g.textFont;
        //float tempsize = g.textSize;

        //graphics = frame.getGraphics();
        //graphics.setFont(baseFont);

        //println(g.textFont.width('\n'));
        //println(g.textFont.getSize());

        //SunGraphics2D holo = ((SunGraphics2D) g.getNative()).getFontMetrics().stringWidth("coucou les loulou");

        //println(graphics.getFontMetrics());
        //((SunGraphics2D) g.getNative()).getFontMetrics().stringWidth("coucou les loulouu")

    }


    public void draw(){
        if (mouseButton == LEFT){
            in_search_of_top_to_dock = false;
            index_second_sommet_to_dock_if_released = -1;
            index_first_sommet_to_dock_if_released = -1;
        }

        ui_manager.draw();
        graphe_manager.drawCurentGraphe();
        debuger.draw();
    }


    public class GrapheManager{
        private Liste<Graphe> graphes;
        private int graphe_amount;
        private int curent_graphe;

        public GrapheManager(){
            this.graphes = new Liste<Graphe>();
            this.graphe_amount = 0;
            this.curent_graphe = 0;
        }

        //Créé un nouveau graphe et le met en onglet principal
        public void newGraphe(int i){
            this.graphes.add(new Graphe(i));
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


    public class UIManager {

        public int couleur_texte =                 color(255);
        public int couleur_bandeau_supperieur =    color(24, 25, 21);
        public int couleur_onglet_innactive =      color(32, 33, 28);
        public int couleur_onglet_active =         color(40, 41, 35);
        public int couleur_background =            color(40, 41, 35);
        public int couleur_liseret_light =         color(70, 70, 70);
        public int couleur_liseret_dark =          color(0);
        public int couleur_bandeau_inferieur =     color(20, 20, 17);

        private int onglet_selected_at_X = -1;
        private int onglet_selected_at_Y = -1;

        public UIManager(){
            /* Créé la barre de menu suppérieur de mon programme (Fichier Edition ...) */
            menu = new Menu();
        }

        /* Dessine les onglets ainsi que les aretes en cours de création avec le clic droit de la souris*/
        public void draw(){
            textSize(12);
            curentMemoryUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            this.afficherOnglets();
            this.afficherAretesEnCourDeCreation();
            this.afficherPiedDePage();
            //this.drawRandom();
        }

        private void drawRandom() {
            textSize(12);
            String str = "50";
            float largeur_du_texte = ((SunGraphics2D) g.getNative()).getFontMetrics().stringWidth(str);

            rectMode(CORNER);
            textAlign(LEFT);
            rect(50,50,largeur_du_texte, 5);
            text(str, 50, 70);

        }

        private void afficherPiedDePage() {
            noStroke();
            textSize(10);

            rectMode(CORNER);
            fill(couleur_bandeau_inferieur);
            rect(0, height-20-menu.getHeight(), width, 20);

            fill(couleur_texte);
            textAlign(RIGHT);
            text("Réalisé par Gabriel Blanchot et Maxime Boissout", width - 5, height - menu.getHeight()-7);

            stroke(couleur_liseret_light);
            line(0,height-20-menu.getHeight(),width,height-20-menu.getHeight());


            textAlign(LEFT);
            if (graphe_manager.getCurentGraphe() != null){
                text("Sommets : " + Integer.toString(graphe_manager.getCurentGraphe().getAmountOfSommets())
                        + ", Aretes : " + Integer.toString(graphe_manager.getCurentGraphe().getAmountOfAretes())
                        + "  -  " + graphe_manager.getCurentGraphe().getStringType(), 5, height - menu.getHeight()-7);
            }
            textSize(10);
            textAlign(CENTER);
        }

        /* Dessine les onglets*/
        private void afficherOnglets(){

            background(couleur_background);

            int nombre_de_graphe = graphe_manager.getAmountOfGraphe();

            if (nombre_de_graphe <= 0) {
                menu.grapheClickabke(false);
            } else {
                menu.grapheClickabke(true);
            }

            int largeur_onglet = 150;

            noStroke();
            rectMode(CORNER);
            fill(couleur_bandeau_supperieur);
            rect(0, 0, width, 35);

            /* Dessine autant d'onglet qu'il y a de graphes et prend soin de récupérer le nom de chaques
            * graphes pour nomer l'onglet ainsi
            * Vérifie si le curseur survole l'onglet qui est en train d'étre déssiné */
            for (int i = 0; i < nombre_de_graphe; i++) {

                if (graphe_manager.getNumberOfCurentGrahe()-1 == i) {
                    fill(couleur_onglet_active);
                } else {
                    fill(couleur_onglet_innactive);
                }

                rect(10 + i*largeur_onglet, 5, largeur_onglet, 30, 10, 10, 0, 0);
                textAlign(LEFT);
                fill(couleur_texte);
                textSize(12);
                text(graphe_manager.getGraphe(i).getName(), 10 + i*largeur_onglet + 10, 25);

                /* Vérifie si le curseur survole l'onglet qui est en train d'étre déssiné si jamais la sourie est cliquée
                * alors le graphe a afficher vas changer en conséquence */
                if (mouseY < 35 && mouseY > 0 && mouseX > 10 + i*largeur_onglet && mouseX < 10 + (i+1)*largeur_onglet) {
                    index_onglet_hover = 1;
                    frame.setCursor(hand_corsor);
                    if (mousePressed) {
                        graphe_manager.setCurentGraphe(i+1);
                    }
                }
            }

            /* Si la souris est en dehors de tout les onglets alors il n'y a plus de sommets pouvant étre survolé*/
            if (!(mouseY < 35 && mouseY > 0 && mouseX > 10 && mouseX < 10 + (graphe_manager.getAmountOfGraphe())*largeur_onglet)) {
                index_onglet_hover = -1;
            }

            textAlign(CENTER);
        }

        /* Dessine les aretes en cours de création*/
        private void afficherAretesEnCourDeCreation(){
            if (mousePressed && mouseButton == RIGHT && in_search_of_top_to_dock){

                if (index_second_sommet_to_dock_if_released != -1){
                    this.afficherArete(
                            graphe_manager.getCurentGraphe().getSommet(index_first_sommet_to_dock_if_released),
                            graphe_manager.getCurentGraphe().getSommet(index_second_sommet_to_dock_if_released),
                            "",
                            graphe_manager.getCurentGraphe().getType(), null);
                } else {
                    this.afficherArete(
                            graphe_manager.getCurentGraphe().getSommet(index_first_sommet_to_dock_if_released),
                            null,
                            "",
                            graphe_manager.getCurentGraphe().getType(), null);
                }

            } else if (in_search_of_top_to_dock && index_second_sommet_to_dock_if_released != -1 && index_first_sommet_to_dock_if_released != -1){

                graphe_manager.getCurentGraphe().addAreteDialog(index_first_sommet_to_dock_if_released, index_second_sommet_to_dock_if_released);
                in_search_of_top_to_dock = false;
                index_second_sommet_to_dock_if_released = -1;
                index_first_sommet_to_dock_if_released = -1;
            } else  if (in_search_of_top_to_dock){

                in_search_of_top_to_dock = false;
                index_second_sommet_to_dock_if_released = -1;
                index_first_sommet_to_dock_if_released = -1;
            }
        }

        /* Méthode pour dessiner le triangle en bout de ligne pour faire une fleche */
        private void arrowHead(int xA, int yA, int xB, int yB) {
            float size = 4;
            pushMatrix();
                translate(xB, yB);
                rotate(atan2(yB - yA, xB - xA));
                triangle(- size * 2 , - size, 0, 0, - size * 2, size);
            popMatrix();
        }

        public void afficherArete(@NotNull Sommet sommetInitial, Sommet sommetFinal, String texte, int typeDeGraphe, @Nullable Arete arete){

            float mouseDistToArrete = 50;

            boolean isCliked = false;

            int xA = sommetInitial.getX();
            int yA = sommetInitial.getY();

            int xB = 0;
            int yB = 0;

            int xC = 0;
            int yC = 0;

            int xD = 0;
            int yD = 0;

            if (sommetFinal != null){
                xD = sommetFinal.getX();
                yD = sommetFinal.getY();
            } else if (in_search_of_top_to_dock){
                xD = mouseX;
                yD = mouseY;
            }

            fill(255);
            stroke(255);

            if (sommetInitial == sommetFinal){

                int diameter = 40;

                int radius = diameter/2;

                mouseDistToArrete = dist(mouseX, mouseY, sommetInitial.getX() + 20, sommetInitial.getY()) - radius;

                System.out.println(mouseDistToArrete);

                if (index_sommet_hover == -1 && index_sommet_holded == -1 && !in_search_of_top_to_dock){
                    if (mouseDistToArrete < 5 && mouseDistToArrete > -5 && (arete_hover == null || arete_hover == arete)){
                        arete_hover = arete;
                    } else if (arete_hover == arete){
                        arete_hover = null;
                    }
                } else {
                    arete_hover = null;
                }

                if (arete_hover == arete){
                    stroke(255,50,50);
                    noFill();
                    arc(sommetInitial.getX() + radius, sommetInitial.getY(), diameter, diameter, PI+QUARTER_PI, TWO_PI+3*QUARTER_PI);

                    fill(255,50,50);
                    noStroke();
                    if (typeDeGraphe == Graphe.ORIENTE || typeDeGraphe == Graphe.ORIENTE_LIBELE || typeDeGraphe == Graphe.ORIENTE_PONDERE){
                        this.arrowHead(sommetInitial.getX() + 5, sommetInitial.getY() + 15, sommetInitial.getX()+ 1, sommetInitial.getY() + 7);
                    }

                    if (mouseButton == LEFT){
                        isCliked = true;
                    }

                } else {
                    stroke(255);
                    noFill();
                    arc(sommetInitial.getX() + radius, sommetInitial.getY(), diameter, diameter, PI+QUARTER_PI, TWO_PI+3*QUARTER_PI);

                    fill(255);
                    noStroke();
                    if (typeDeGraphe == Graphe.ORIENTE || typeDeGraphe == Graphe.ORIENTE_LIBELE || typeDeGraphe == Graphe.ORIENTE_PONDERE){
                        this.arrowHead(sommetInitial.getX() + 5, sommetInitial.getY() + 15, sommetInitial.getX()+ 1, sommetInitial.getY() + 7);
                    }
                }

                noFill();
                noStroke();
            }
            else {

                int ecart = 15;

                float distAD = dist(xA, yA, xD, yD);

                int x1 = (int)(xA - (ecart*(xA-xD)/distAD));
                int y1 = (int)(yA + (ecart*(yD-yA)/distAD));

                int x2 = (int)(xA - ((distAD-ecart)*(xA-xD)/distAD));
                int y2 = (int)(yA + ((distAD-ecart)*(yD-yA)/distAD));

                if (!in_search_of_top_to_dock){

                    float dx = x2 - x1;
                    float dy = y2 - y1;

                    float dist = ((mouseX - x1) * dx + (mouseY - y1) * dy) / (dx * dx + dy * dy);

                    if (dist < 0) {
                        dx = mouseX - x1;
                        dy = mouseY - y1;
                    } else if (dist > 1) {
                        dx = mouseX - x2;
                        dy = mouseY - y2;
                    } else {
                        dx = mouseX - (x1 + dist * dx);
                        dy = mouseY - (y1 + dist * dy);
                    }

                    mouseDistToArrete = sqrt(dx * dx + dy * dy);

                    if (index_sommet_hover == -1 && index_sommet_holded == -1){
                        if (mouseDistToArrete < 5 && distAD > 2*ecart && (arete_hover == null || arete_hover == arete)){
                            arete_hover = arete;
                        } else if (arete_hover == arete){
                            arete_hover = null;
                        }
                    } else {
                        arete_hover = null;
                    }

                    if (arete_hover == arete){
                        fill(255,50,50);
                        stroke(255,50,50);

                        if (mouseButton == LEFT){
                            isCliked = true;
                        }
                    }


                }

                if (typeDeGraphe == Graphe.ORIENTE_LIBELE || typeDeGraphe == Graphe.ORIENTE_PONDERE || typeDeGraphe == Graphe.NON_ORIENTE_LIBELE || typeDeGraphe == Graphe.NON_ORIENTE_PONDERE){

                    float longueurLigne = dist(x1, y1, x2, y2);
                    float longueureDuTexte = ((SunGraphics2D) g.getNative()).getFontMetrics().stringWidth(texte);

                    int ecartTexte = 5;

                    float longueurSegment = longueurLigne/2-longueureDuTexte/2;

                    xB = (int)(xA - ((ecart + longueurSegment - ecartTexte)*(xA-xD)/distAD));
                    yB = (int)(yA + ((ecart + longueurSegment - ecartTexte)*(yD-yA)/distAD));

                    xC = (int)(xA - ((distAD-ecart-longueurSegment + ecartTexte)*(xA-xD)/distAD));
                    yC = (int)(yA + ((distAD-ecart-longueurSegment + ecartTexte)*(yD-yA)/distAD));

                    int xTexte = (int)(xA - ((ecart+longueurSegment+longueureDuTexte/2)*(xA-xD)/distAD));
                    int yTexte = (int)(yA + ((ecart+longueurSegment+longueureDuTexte/2)*(yD-yA)/distAD));


                    if (dist(xA, yA, xD, yD) <= 2*ecart+2){
                        line(xA, yA, xD, yD);
                    } else if (texte.equals("") || (dist(x1, y1, x2, y2) < ecartTexte*2+longueureDuTexte)){
                        line(x1, y1, x2, y2);
                    } else {
                        line(x1, y1, xB, yB);
                        line(xC, yC, x2, y2);
                        pushMatrix();
                            translate(xB, yB);
                            if (atan2(yB - yC, xB - xC) > (-PI/2) && atan2(yB - yC, xB - xC) < (PI/2)){
                                rotate(atan2(yB - yC, xB - xC));
                                text(texte, 0-longueureDuTexte/2-ecartTexte, 3);
                            } else {
                                rotate(atan2(yC - yB, xC - xB));
                                text(texte, longueureDuTexte/2+ecartTexte, 3);
                            }
                        popMatrix();
                    }

                }
                else {
                    if (dist(xA, yA, xD, yD) <= 2*ecart+2){
                        line(xA, yA, xD, yD);
                    } else {
                        line(x1, y1, x2, y2);
                    }
                }

                if ((typeDeGraphe == Graphe.ORIENTE || typeDeGraphe == Graphe.ORIENTE_LIBELE || typeDeGraphe == Graphe.ORIENTE_PONDERE) && distAD > ecart*2+2){
                    arrowHead(x1, y1, x2, y2);
                }
            }

            if (isCliked && !in_search_of_top_to_dock){
                Dialog dial = new Dialog<Integer, Integer, Float>(
                        "Changer valeur arrete",
                        "Sommet de départ de l'arête :",
                        "Sommet de fin de l'arête :",
                        "Coût de l'arête :",
                        Integer.class,
                        Integer.class,
                        Float.class);

                /* On réculère les valeurs que l'utilisateur a rentré*/
                if (dial.getA() != null && dial.getB() != null && dial.getC() != null) {
                    int a = (int)dial.getA();
                    int b = (int)dial.getB();
                    float c = (float)dial.getC();
                }
            }

            noStroke();
        }
    }


    /* Classe régissant la création de la barre de menu suppérieure*/
    public class Menu extends JFrame implements ActionListener {

        private int height = 0;

        /* Déclaration de tout les élèments de mon menu*/
        private JMenuItem action_nouveau_graphe_oriente;
        private JMenuItem action_nouveau_graphe_oriente_pondere;
        private JMenuItem action_nouveau_graphe_oriente_etiquete;
        private JMenuItem action_nouveau_graphe_non_oriente;
        private JMenuItem action_nouveau_graphe_non_oriente_pondere;
        private JMenuItem action_nouveau_graphe_non_oriente_etiquete;

        private JMenuItem action_ouvrir_un_graphe;
        private JMenuItem action_fermer_ce_graphe;
        private JMenuItem action_renommer_ce_graphe;
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
        private JMenuItem action_activer_debogueur;

        private JMenuItem action_mise_a_jour;
        private JMenuItem action_documentation;

        private JPanel panel;

        /* Constructeur */
        public Menu() {

            frame.setLocationRelativeTo(null);
            frame.setMinimumSize(new Dimension(490,200));
            JMenuBar barre = new JMenuBar();

            frame.setJMenuBar(barre);

            /* La grande barre de menu (la JMenuBar) est composée de plusieurs autres menu (les JMenu), on leur donne
            * un texte a afficher (Fichier, Edition ...)*/

            JMenu fichier_menu = new JMenu("Fichier");
            JMenu edition_menu = new JMenu("Édition");
            JMenu outils_menu = new JMenu("Outils");
            JMenu graphe_menu = new JMenu("Graphe");
            JMenu preference_menu = new JMenu("Préférences");
            JMenu aide_menu = new JMenu("Aide");

            /* On ajoute a la grande barre ses menu*/
            barre.add(fichier_menu);
            barre.add(edition_menu);
            barre.add(outils_menu);
            barre.add(graphe_menu);
            barre.add(preference_menu);
            barre.add(aide_menu);

            // --------------------------------------------------------------


            /* Un menu (fichier par exemple ) peux comporter d'autres menu, ou simplement il peux comporter les "boutons"
            * qui exécuterons des taches (les JMenuItem)
            * Ici on défini ceux du menu Fichier*/
            action_nouveau_graphe_oriente = new JMenuItem("Graphe Orienté");
            action_nouveau_graphe_oriente_pondere = new JMenuItem("Graphe Orienté Pondéré");
            action_nouveau_graphe_oriente_etiquete = new JMenuItem("Graphe Orienté Étiqueté");
            action_nouveau_graphe_non_oriente = new JMenuItem("Graphe Non Orienté");
            action_nouveau_graphe_non_oriente_pondere = new JMenuItem("Graphe Non Orienté Pondéré");
            action_nouveau_graphe_non_oriente_etiquete = new JMenuItem("Graphe Non Orienté Étiqueté");
            action_ouvrir_un_graphe = new JMenuItem("Ouvrir un Graphe");
            action_renommer_ce_graphe = new JMenuItem("Renommer ce Graphe");
            action_fermer_ce_graphe = new JMenuItem("Fermer ce Graphe");
            action_fermer_tout_les_graphes = new JMenuItem("Fermer tout les Graphes");
            action_enregistrer_sous = new JMenuItem("Enregistrer sous");
            action_imprimer = new JMenuItem("Imprimer ce Graphe");
            action_quitter = new JMenuItem("Quitter");

            /* Puis on leur ajoute un écouteur, si une action est recensé sur un de ces boutons (si un bouton est cliqué)
            * alors la méthode ActionPerformed (plus bas) sera exécutée et aura en parametre le bouton qui aura été cliqué
            * nous pourrons alors exécuter l'action correspondante au bouton*/
            action_nouveau_graphe_oriente.addActionListener(this);
            action_nouveau_graphe_oriente_etiquete.addActionListener(this);
            action_nouveau_graphe_oriente_pondere.addActionListener(this);
            action_nouveau_graphe_non_oriente.addActionListener(this);
            action_nouveau_graphe_non_oriente_etiquete.addActionListener(this);
            action_nouveau_graphe_non_oriente_pondere.addActionListener(this);
            action_ouvrir_un_graphe.addActionListener(this);
            action_renommer_ce_graphe.addActionListener(this);
            action_fermer_ce_graphe.addActionListener(this);
            action_fermer_tout_les_graphes.addActionListener(this);
            action_enregistrer_sous.addActionListener(this);
            action_imprimer.addActionListener(this);
            action_quitter.addActionListener(this);

            JMenu nouveau_menu = new JMenu("Nouveau");

            /* Ici on met en forme le menu, on ajoute quelques séparateurs pour l'estétique*/
            nouveau_menu.add(action_nouveau_graphe_oriente);
            nouveau_menu.add(action_nouveau_graphe_oriente_pondere);
            nouveau_menu.add(action_nouveau_graphe_oriente_etiquete);
            nouveau_menu.addSeparator();
            nouveau_menu.add(action_nouveau_graphe_non_oriente);
            nouveau_menu.add(action_nouveau_graphe_non_oriente_pondere);
            nouveau_menu.add(action_nouveau_graphe_non_oriente_etiquete);
            fichier_menu.add(nouveau_menu);
            fichier_menu.addSeparator();
            fichier_menu.add(action_ouvrir_un_graphe);
            fichier_menu.add(action_fermer_ce_graphe);
            fichier_menu.add(action_fermer_tout_les_graphes);
            fichier_menu.addSeparator();
            fichier_menu.add(action_renommer_ce_graphe);
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
            action_activer_debogueur = new JMenuItem("Activer le Débogueur");

            action_theme.addActionListener(this);
            action_activer_debogueur.addActionListener(this);

            preference_menu.add(action_theme);
            preference_menu.addSeparator();
            preference_menu.add(action_activer_debogueur);

            // --------------------------------------------------------------

            action_mise_a_jour = new JMenuItem("Mise à jour");
            action_documentation = new JMenuItem("Documentation");

            action_mise_a_jour.addActionListener(this);
            action_documentation.addActionListener(this);

            aide_menu.add(action_mise_a_jour);
            aide_menu.addSeparator();
            aide_menu.add(action_documentation);

            // --------------------------------------------------------------


            /**/
            frame.setVisible(true);

            this.height = barre.getHeight();
        }

        public int getHeight(){
            return this.height;
        }

        public void actionPerformed(ActionEvent e) {

            Object src = e.getSource();

            if (src == action_ajouter_un_sommet) {
                println("Ajouter un nouveau sommet");
                graphe_manager.getCurentGraphe().addSommet();


            }

            else if (src == action_renommer_ce_graphe){
                /* Création d'une instance de Dialog, avec tout les parametres qui vont avec, tant que l'utilisateur n'aura pas
                 * cliqué sur OK ou qu'il aura cliqué sur annuler le code attend a ce niveau*/
                Dialog dial = new Dialog<String, NullType, NullType>(
                        "Renommer ce graphe",
                        "Nouveau nom :",
                        "",
                        "",
                        String.class,
                        NullType.class,
                        NullType.class);

                /* On réculère les valeurs que l'utilisateur a rentré*/
                if (dial.getA() != null) {
                    String a = (String)dial.getA();

                    graphe_manager.getCurentGraphe().setName(a);
                }
            }

            else if (src == action_ajouter_une_arete) {

                /* Création d'une instance de Dialog, avec tout les parametres qui vont avec, tant que l'utilisateur n'aura pas
                * cliqué sur OK ou qu'il aura cliqué sur annuler le code attend a ce niveau*/
                Dialog dial = new Dialog<Integer, Integer, Float>(
                        "Nouvelle arête",
                        "Sommet de départ de l'arête :",
                        "Sommet de fin de l'arête :",
                        "Coût de l'arête :",
                        Integer.class,
                        Integer.class,
                        Float.class);

                /* On réculère les valeurs que l'utilisateur a rentré*/
                if (dial.getA() != null && dial.getB() != null && dial.getC() != null) {
                    int a = (int)dial.getA();
                    int b = (int)dial.getB();
                    float c = (float)dial.getC();

                    graphe_manager.getCurentGraphe().addArete(a, b, c);
                }


            }

            else if (src == action_supprimer_une_arete) {
                System.out.println("Supprimer une arête");


            }

            else if (src == action_supprimer_un_sommet) {
                System.out.println("Supprimer un sommet");


            }

            else if (src == action_calcul_matrice_transitive) {
                System.out.println("Calcul de la matrice transitive");


            }

            else if (src == action_calcul_matrice_adjacente) {
                System.out.println("Calcul de la matrice adjacente");


            }

            else if (src == action_quitter) {
                exit();
            }

            else if (src == action_nouveau_graphe_oriente) {
                graphe_manager.newGraphe(Graphe.ORIENTE);
            }

            else if (src == action_nouveau_graphe_oriente_etiquete) {
                graphe_manager.newGraphe(Graphe.ORIENTE_LIBELE);
            }

            else if (src == action_nouveau_graphe_oriente_pondere) {
                graphe_manager.newGraphe(Graphe.ORIENTE_PONDERE);
            }

            else if (src == action_nouveau_graphe_non_oriente) {
                graphe_manager.newGraphe(Graphe.NON_ORIENTE);
            }

            else if (src == action_nouveau_graphe_non_oriente_etiquete) {
                graphe_manager.newGraphe(Graphe.NON_ORIENTE_LIBELE);
            }

            else if (src == action_nouveau_graphe_non_oriente_pondere) {
                graphe_manager.newGraphe(Graphe.NON_ORIENTE_PONDERE);
            }

            else if (src == action_activer_debogueur){
                if(!debuger.isActive){
                    debuger.enableDebuger();
                    action_activer_debogueur.setText("Désactiver le Debogueur");
                } else {
                    debuger.disableDebuger();
                    action_activer_debogueur.setText("Activer le Débogueur");
                }

            }

        }

        /* Je rend cliquable ou pas les boutons du menu, en effet, si il n'existe pas de graphe, rien ne sert
        * d'ajouter une arete ou un sommet, encore moins d'essayer d'en supprimer un*/
        public void grapheClickabke(boolean b){
            action_ajouter_une_arete.setEnabled(b);
            action_ajouter_un_sommet.setEnabled(b);
            action_supprimer_une_arete.setEnabled(b);
            action_supprimer_un_sommet.setEnabled(b);
            action_modifier_une_arete.setEnabled(b);
            action_renommer_ce_graphe.setEnabled(b);
            action_fermer_ce_graphe.setEnabled(b);
            action_fermer_tout_les_graphes.setEnabled(b);
            action_calcul_matrice_adjacente.setEnabled(b);
            action_calcul_matrice_transitive.setEnabled(b);
            action_imprimer.setEnabled(b);
            action_enregistrer_sous.setEnabled(b);
        }
    }


    /* Petite classe me permettant de mettre mon curseur sur un champs de texte
    * Je l'utilise pour régler un petit problème de ma fenetre de Dialogue, je voulais que mon curseur sois dirrectement
    * dans la première cellule quand j'ouvre une fenetre*/
    public static class FocusRequest implements AncestorListener {

        private boolean removeListener;

        public FocusRequest() {
            this(true);
        }

        public FocusRequest(boolean removeListener) {
            this.removeListener = removeListener;
        }

        @Override
        public void ancestorAdded(AncestorEvent e) {
            JComponent component = e.getComponent();
            component.requestFocusInWindow();

            if (removeListener) {
                component.removeAncestorListener(this);
            }
        }

        @Override
        public void ancestorMoved(AncestorEvent e) {

        }

        @Override
        public void ancestorRemoved(AncestorEvent e) {

        }
    }

    public class Debuger {
        public boolean isActive = false;

        public Debuger(){

        }

        public void disableDebuger(){
            this.isActive = false;
        }

        public void enableDebuger(){
            this.isActive = true;
        }

        public void draw(){
            if (this.isActive){

                fill(255);
                textAlign(LEFT);

                int i = 0;
                text("fps : " + nf.format(frameRate), 20, 50 + 20 *i);
                i++;
                text("Mémoire utilisée (pgrm) : " + nf.format(((curentMemoryUsed-startMemoryUsed)/1000000.0)) + " Mo", 20,50 + 20*i);
                i++;
                text("Mémoire utilisée (total) : " + nf.format(((curentMemoryUsed/1000000.0))) + " Mo", 20,50 + 20 *i);
                i++;
                i++;
                text("Sommet Survolé : " + Integer.toString(index_sommet_hover), 20, 50 + 20 *i);
                i++;
                text("Sommet sélectionné : " + Integer.toString(index_sommet_holded), 20, 50 + 20 *i);
                i++;
                i++;
                text("Arete survolée : " + arete_hover, 20, 50 + 20 *i);
                i++;
                i++;
                text("En train de relier des sommets ? : " + Boolean.toString(in_search_of_top_to_dock), 20, 50 + 20 *i);
                i++;
                text("1er Sommet à relier : " + Integer.toString(index_first_sommet_to_dock_if_released), 20, 50 + 20 *i);
                i++;
                text("2nd Sommet à relier : " + Integer.toString(index_second_sommet_to_dock_if_released), 20, 50 + 20 *i);
                i++;
                text("Onglet Survolé : " + Integer.toString(index_onglet_hover), 20, 50 + 20 *i);
            }
        }
    }
}
