package it.polito.tdp.seriea.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.model.Match;
import it.polito.tdp.seriea.model.Season;
import it.polito.tdp.seriea.model.Team;

public class SerieADAO {

	public List<Season> listAllSeasons() {
		String sql = "SELECT season, description FROM seasons";
		List<Season> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Season(res.getInt("season"), res.getString("description")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public List<Team> listTeams() {
		String sql = "SELECT team FROM teams";
		List<Team> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Team(res.getString("team")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	// Metodo che mi restituisce l'insieme delle partite giocate da una determinata squadra
	
	public List<Match> listMatchesForTeam(Team squadra,
			Map<Integer, Season> stagioniIdMap,
			Map<String, Team> squadreIdMap) {
		
		String sql = "SELECT match_id, season, 'div', date, hometeam, awayteam, fthg, ftag, ftr " + 
				"FROM matches " + 
				"WHERE HomeTeam = ? OR AwayTeam = ?";
		List<Match> result = new ArrayList<>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, squadra.getTeam());
			st.setString(2, squadra.getTeam());
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Match match = new Match(
						res.getInt("match_id"),
						stagioniIdMap.get(res.getInt("season")), // trasformo l'intero nell'oggetto season corrispondente
						res.getString("div"),
						res.getDate("date").toLocalDate(),
						squadreIdMap.get(res.getString("hometeam")),
						squadreIdMap.get(res.getString("awayteam")),
						res.getInt("fthg"),
						res.getInt("ftag"),
						res.getString("ftr"));
				result.add(match);
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

}
