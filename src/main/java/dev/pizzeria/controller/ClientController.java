package dev.pizzeria.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.eclipse.jetty.io.ssl.ALPNProcessor.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.pizzeria.dao.ClientsDAO;
import dev.pizzeria.exception.FormException;
import dev.pizzeria.model.Client;

/**
 * Classe de gestion d'un client de la pizzeria
 */
public class ClientController extends HttpServlet {

	/** Logger LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
	/**
	 * boolean isFromACreatingError : indique s'il y a eu une précédente erreur lors
	 * de l'envoi du formulaire de création
	 */
	private boolean isFromACreatingError = false;
	/**
	 * String TEMPLATE_CLIENT_CREER : spécifie l'adresse du template HTML qui
	 * servira à générer la réponse
	 */
	public static final String TEMPLATE_CLIENT_CREER = "templates/client.html";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			ajouterUnClient(req);
			resp.sendRedirect("/clients");
		} catch (FormException e) {
			resp.setStatus(400);
			LOGGER.info("Erreur d'insertion du client : " + e);
			isFromACreatingError = true;

			resp.sendRedirect("/client");
		}
	}

	/**
	 * Ajoute un client à la base de données (List) de la pizzeria
	 * 
	 * @param req
	 * @throws FormException
	 */
	public void ajouterUnClient(HttpServletRequest req) throws FormException {
		try {
			String nom = req.getParameter("nom");
			String prenom = req.getParameter("prenom");
			String ville = req.getParameter("ville");
			int age = Integer.parseInt(req.getParameter("age"));

			if (nom.length() > 0 && prenom.length() > 0 && ville.length() > 0 && age > 0) {
				ClientsDAO.clients.add(new Client(nom, prenom, ville, age));
			} else {
				throw new FormException("un des champs est vide.");
			}
		} catch (Exception f) {
			throw new FormException("erreur importante dans le formulaire (de type ou nullpointer).");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			resp.setCharacterEncoding("UTF-8");

			String template = Files
					.readAllLines(
							Paths.get(this.getClass().getClassLoader().getResource(TEMPLATE_CLIENT_CREER).toURI()))
					.stream().collect(Collectors.joining());
			if (isFromACreatingError) {
				template = template.replaceAll("<msgSiErreur></msgSiErreur>",
						"<p style=\"color:orange\"><b>ERREUR : MERCI DE RECOMMENCER</b></p>");
			}

			PrintWriter writer = resp.getWriter();
			writer.write(template);
			isFromACreatingError = false;

		} catch (URISyntaxException e) {
			LOGGER.error("Fichier HTML non trouvé", e);
			resp.setStatus(404);
		}
	}

}
