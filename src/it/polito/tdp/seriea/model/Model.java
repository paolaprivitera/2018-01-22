package it.polito.tdp.seriea.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialArray;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {

	private List<Team> squadre;
	private Map<String, Team> squadreIdMap;

	private List<Season> stagioni;
	private Map<Integer, Season> stagioniIdMap;

	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi; // Per ogni stagione devo calcolare il punteggio

	private Graph<Season, DefaultWeightedEdge> grafo;
	
	public Model() {
		SerieADAO dao = new SerieADAO();

		this.squadre = dao.listTeams();

		this.squadreIdMap = new HashMap<String, Team>();
		for(Team t : this.squadre) {
			this.squadreIdMap.put(t.getTeam(), t);
		}


		this.stagioni = dao.listAllSeasons();

		this.stagioniIdMap = new HashMap<Integer, Season>();
		for(Season s : this.stagioni) {
			this.stagioniIdMap.put(s.getSeason(), s);
		}

	}


	public List<Team> getSquadre() {
		return this.squadre;
	}

	public Map<Season, Integer> calcolaPunteggi(Team squadra) {

		this.squadraSelezionata = squadra;
		
		this.punteggi = new HashMap<Season, Integer>();

		SerieADAO dao = new SerieADAO();

		List<Match> partite = dao.listMatchesForTeam(squadra, stagioniIdMap, squadreIdMap);

		// Per ogni partita della mappa devo aggiornare i risultati
		for(Match m : partite) {
			Season stagione = m.getSeason();

			int punti = 0;

			if(m.getFtr().equals("D")) {
				punti = 1;
			}
			else {
				if((m.getHomeTeam().equals(squadra)) && (m.getFtr().equals("H")) || 
						(m.getAwayTeam().equals(squadra)) && (m.getFtr().equals("A"))) {
					punti = 3;
				}
			}
			
			// int attuale = punteggi.get(stagione);
			
			// nella prima partita di ogni stagione la mappa punteggi e' vuota
			// quindi punteggi.get(stazione) mi ritorna un null che poi convertito in intero da' un'eccezione
			// allora lavoro con oggetti -> Integer
			// e faccio un ulteriore controllo se mi trovo nella prima partita della stagione
			
			// oppure verifico con una contains se la stagione e' contenuta nella mappa
			
			// oppure potrei inizializzare la mappa con tutte le stagioni e punteggio 0
			// MA non riuscirei a capire in quali stagioni una squadra non ha giocato
			
			Integer attuale = punteggi.get(stagione);
			
			if(attuale == null)
				attuale = 0;
			
			punteggi.put(stagione, attuale+punti);			
			
			
		}
		
		return this.punteggi;
	}
	
	public Season calcolaAnnataDOro() {
		
		// Costruisco il grafo
		this.grafo = new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(this.grafo, punteggi.keySet()); 
		// al posto di punteggi.keySet() non posso usare la lista stagioni perche'
		// sono comprese anche le stagioni in cui la squadra non ha giocato
		// percio' utilizzo le chiavi della mappa punteggi
		
		for(Season s1 : punteggi.keySet()) { // oppure grafo.vertexSet()
			for(Season s2 : punteggi.keySet()) {
				if(!s1.equals(s2)) { // creo l'arco solo se le stagioni sono diverse
									 // sarebbe meglio controllare che s1 sia minore di s2 cosi'
									 // li controllerei soltanto una volta
					if(punteggi.get(s1) > punteggi.get(s2)) {
						Graphs.addEdge(this.grafo, s2, s1, punteggi.get(s1)-punteggi.get(s2));
					}
					else {
						Graphs.addEdge(this.grafo, s1, s2, punteggi.get(s2)-punteggi.get(s1));
						// se le stagioni hanno lo stesso punteggio in questo modo aggiungero' due archi con peso 0
						// perche' ci sono due cicli for
					}
				}
			}
		}
		
		// trovo l'annata migliore
		Season migliore = null;
		
		int max = 0;
		
		for(Season s : grafo.vertexSet()) {
			// per ogni vertice calcolo il peso
			int valore = pesoStagione(s);
			if(valore > max) {
				max = valore;
				migliore = s;
			}
		}
		
		return migliore;
		
		
		
	}


	private int pesoStagione(Season s) {
		
		int somma = 0;
		
		for(DefaultWeightedEdge e : this.grafo.incomingEdgesOf(s)) {
			somma += grafo.getEdgeWeight(e);
		}
		
		for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(s)) {
			somma -= grafo.getEdgeWeight(e);
		}
		
		return somma;
	}

}
