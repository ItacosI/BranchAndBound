package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* Les Tests pour fichier "objets.txt"
	( première ligne : capacité sac )
	( autres : poids utilité		)
	
50
10 60
20 100
30 120


17
3 8
7 18
9 20
6 11

 */

public class main {

	public static int capaciteSac, ub, lb;
	public static List<Obj> objets;
	public static float[] sol;

	public static float[] cleanClone(float[] src) {
		float[] dest = new float[src.length];
		for (int i = 0; i < src.length; i++) {
			dest[i] = Float.valueOf(src[i]);
		}
		return dest;
	}

	/**
	 * Branch and Bound
	 * 
	 * @param objetPlaces représente les objets à placer dans le sac (dans l'ordre
	 *                    décroissant de ratio) avec 3 codes : 1 pour un objet a
	 *                    mettre -1 pour un objet a ne pas mettre 0 pour les autres
	 */
	public static boolean bandb(float[] objetPlaces) {

		int tmpPlace = capaciteSac;
		int tmpUB = 0;
		for (int i = 0; i < objetPlaces.length; i++) { // On s'occupe des objets obligatoire
			if (objetPlaces[i] == 1) { // on met l'objet à l'index i
				Obj o = objets.get(i);
				tmpPlace -= o.getPoids();
				tmpUB += o.getUtils();
			}
		}

		int objetFraction = -1;
		for (int i = 0; i < objets.size(); i++) { // On regarde les autres objets
			if (objetPlaces[i] == 0) { // != 1 ou -1
				Obj o = objets.get(i);
				if (tmpPlace - o.getPoids() >= 0) { // si on peut faire rentrer l'objet dans le sac
					tmpPlace -= o.getPoids();
					tmpUB += o.getUtils();

				} else if (tmpPlace >= 0) { // sinon c'est le dernier objet qu'on regarde si le sac n'est pas plein et
											// on le fractionne
					tmpUB += tmpPlace / (float) o.getPoids() * o.getUtils();
					objetFraction = i;
					break;
				}
			}
		}

		if (lb > tmpUB) // On ne poursuit pas sur ce noeud
			return false;

		if (lb == tmpUB) { // Si UB du noeud = LB --> on a la solution
			ub = tmpUB;
			sol = cleanClone(objetPlaces);
			return true;
		}

		if (lb < tmpUB) { // Si la borne UB de ce noeud est > à la borne LB globale
			if (objetFraction == -1) { // Si le résultat est entier, on update la LB globale
				lb = tmpUB;
				sol = cleanClone(objetPlaces);
				return false;
			}
			ub = tmpUB; // On met à jour UB

			// Sans l'objet fractionné
			objetPlaces[objetFraction] = -1; // On retire l'objet fractionné
			if (bandb(objetPlaces))
				return true;
			else {
				// Avec l'objet fractionné
				objetPlaces[objetFraction] = 1; // On ajoute l'objet fractionné
				if (bandb(objetPlaces))
					return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {

		capaciteSac = 0;
		objets = new ArrayList<Obj>();

		try {
			int cpt = 0;
			for (String ligne : Files.readAllLines(Paths.get("src/main/objets.txt"))) {
				if (cpt == 0) {
					capaciteSac = Integer.valueOf(ligne);
				} else {
					String[] chaine = ligne.split(" ");
					objets.add(new Obj(cpt, Integer.valueOf(chaine[0]), Integer.valueOf(chaine[1])));
				}
				cpt++;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Capacité du sac : " + capaciteSac);
		System.out.println("\n¤ Liste non triée ¤");
		for (Obj o : objets) {
			System.out.println(o.toString());
		}

		Comparator<Obj> parUtils = Comparator.comparing(Obj::getInvertUtils);
		Collections.sort(objets, parUtils); // tri par Utils décroissant

		System.out.println("\n¤ Liste triée par utils ¤");
		for (Obj o : objets) {
			System.out.println(o.toString());
		}

		lb = 0;
		int restePoids = capaciteSac;

		for (Obj o : objets) { // Calcul borne inf
			if (restePoids - o.getPoids() >= 0) {
				restePoids -= o.getPoids();
				lb += o.getUtils();
			} else {
				break;
			}
		}

		System.out.println("====> lb = " + lb);

		Comparator<Obj> parRatio = Comparator.comparing(Obj::getInvertRatio);
		Collections.sort(objets, parRatio); // tri par ratio décroissant

		System.out.println("\n¤ Liste triée par ratio ¤");
		for (Obj o : objets) {
			System.out.println(o.toString());
		}

		ub = 0;
		restePoids = capaciteSac;
		Obj aFractionner = null;
		for (int i = 0; i < objets.size(); i++) { // Calcul borne sup
			Obj o = objets.get(i);
			if (restePoids - o.getPoids() >= 0) {
				restePoids -= o.getPoids();
				ub += o.getUtils();
			} else { // Stockage de l'objet a fractionner pour Fayard et Plateau
				aFractionner = o;
				break;
			}
		}
		if (aFractionner != null)
			ub += (restePoids / (float) aFractionner.getPoids()) * aFractionner.getUtils();// maximisation du contenu du
																							// sac

		System.out.println("====> ub = " + ub);

		sol = new float[objets.size()];
		for (int i = 0; i < sol.length; i++) {
			sol[i] = 0;
		}

		bandb(cleanClone(sol)); // Lancement de Branch and Bound

		int poids = 0;
		System.out.println("\n====================================\nUtilité max : " + lb);
		System.out.println("¤ Composition du sac ¤");
		for (int i = 0; i < sol.length; i++) {
			if (sol[i] == 1) {
				Obj o = objets.get(i);
				poids += o.getPoids();
				System.out.println(o.toString());
			}
		}
		System.out.println("Poids total : " + poids + "\n====================================");

	}

}
