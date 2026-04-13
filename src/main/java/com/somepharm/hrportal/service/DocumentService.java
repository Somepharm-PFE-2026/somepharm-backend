package com.somepharm.hrportal.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.somepharm.hrportal.entity.Utilisateur;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentService {

    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
    private final Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(37, 99, 235));

    // --- 1. ATTESTATION DE TRAVAIL ---
    public byte[] genererAttestationTravail(Utilisateur employe) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addCompanyHeader(document);

            Paragraph title = new Paragraph("ATTESTATION DE TRAVAIL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Paragraph body = new Paragraph();
            body.setFont(textFont);
            body.setLeading(25f);
            body.add("Nous soussignés, la Direction des Ressources Humaines de la société ");
            body.add(new Chunk("SOMEPHARM", boldFont));
            body.add(", attestons par la présente que :\n\n");
            body.add("L'employé(e) identifié(e) par le matricule : ");
            body.add(new Chunk(employe.getMatricule(), boldFont));
            body.add("\nDépartement : ");
            body.add(new Chunk(employe.getDepartement(), boldFont));

            String roleNom = employe.getRole() != null ? employe.getRole().getNomRole() : "Employé";
            body.add("\nFonction occupée : ");
            body.add(new Chunk(roleNom, boldFont));

            body.add("\n\nEst actuellement salarié(e) au sein de notre structure et exerce ses fonctions de manière régulière à ce jour.");
            body.add("\nCette attestation lui est délivrée sur sa demande pour servir et valoir ce que de droit.");

            document.add(body);
            document.add(Chunk.NEWLINE);
            addSignature(document);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // --- 2. FICHE DE PAIE (PAYSLIP) ---
    public byte[] genererFicheDePaie(Utilisateur employe) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addCompanyHeader(document);

            Paragraph title = new Paragraph("BULLETIN DE PAIE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            Paragraph period = new Paragraph("Période : " + currentMonth, boldFont);
            period.setAlignment(Element.ALIGN_CENTER);
            document.add(period);
            document.add(Chunk.NEWLINE);

            // Employee Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(createCell("Matricule: " + employe.getMatricule(), false));
            infoTable.addCell(createCell("Département: " + employe.getDepartement(), false));
            infoTable.addCell(createCell("Email: " + employe.getEmail(), false));
            String roleNom = employe.getRole() != null ? employe.getRole().getNomRole() : "Employé";
            infoTable.addCell(createCell("Fonction: " + roleNom, false));
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Salary Details Table (Mock Data for PFE Presentation)
            PdfPTable salaryTable = new PdfPTable(2);
            salaryTable.setWidthPercentage(100);
            salaryTable.addCell(createCell("Désignation", true));
            salaryTable.addCell(createCell("Montant (DZD)", true));

            salaryTable.addCell(createCell("Salaire de Base", false));
            salaryTable.addCell(createCell("85,000.00", false));

            salaryTable.addCell(createCell("Prime de Panier", false));
            salaryTable.addCell(createCell("5,000.00", false));

            salaryTable.addCell(createCell("Retenue SS (9%)", false));
            salaryTable.addCell(createCell("-7,650.00", false));

            salaryTable.addCell(createCell("IRG", false));
            salaryTable.addCell(createCell("-12,400.00", false));

            PdfPCell netCell1 = createCell("NET À PAYER", true);
            PdfPCell netCell2 = createCell("69,950.00 DZD", true);
            netCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            netCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            salaryTable.addCell(netCell1);
            salaryTable.addCell(netCell2);

            document.add(salaryTable);
            document.add(Chunk.NEWLINE);
            addSignature(document);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    // --- 3. ATTESTATION DE SALAIRE (Pour Banques / Visas) ---
    public byte[] genererAttestationSalaire(Utilisateur employe) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addCompanyHeader(document);

            Paragraph title = new Paragraph("ATTESTATION DE SALAIRE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Paragraph body = new Paragraph();
            body.setFont(textFont);
            body.setLeading(25f);
            body.add("Nous soussignés, la Direction Financière et RH de ");
            body.add(new Chunk("SOMEPHARM", boldFont));
            body.add(", attestons que :\n\n");

            body.add("M/Mme : ");
            body.add(new Chunk(employe.getMatricule(), boldFont));
            body.add("\nFonction : " + (employe.getRole() != null ? employe.getRole().getNomRole() : "Employé"));

            body.add("\n\nPerçoit un salaire mensuel net de : ");
            body.add(new Chunk("69,950.00 DZD", boldFont)); // Mock data for PFE
            body.add(" (Soixante-Neuf Mille Neuf Cent Cinquante Dinars Algériens).");

            body.add("\n\nNous certifions qu'à ce jour, son salaire n'est grevé d'aucune opposition ni saisie-arrêt.");
            body.add("\nCette attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit auprès des institutions bancaires et administratives.");

            document.add(body);
            document.add(Chunk.NEWLINE);
            addSignature(document);
            document.close();
        } catch (DocumentException e) { e.printStackTrace(); }
        return out.toByteArray();
    }

    // --- 4. TITRE DE CONGÉ (Preuve de vacances) ---
    public byte[] genererTitreConge(Utilisateur employe) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addCompanyHeader(document);

            Paragraph title = new Paragraph("TITRE DE CONGÉ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Paragraph body = new Paragraph();
            body.setFont(textFont);
            body.setLeading(25f);

            body.add("Il est accordé à l'employé(e) soussigné(e) :\n\n");
            body.add("Matricule : ");
            body.add(new Chunk(employe.getMatricule(), boldFont));
            body.add("\nDépartement : " + employe.getDepartement());

            body.add("\n\nUn congé réglementaire de l'année en cours.");
            body.add("\nSolde de congé actuel restant : ");
            body.add(new Chunk(employe.getSoldeConges() + " Jours", boldFont));

            body.add("\n\nL'employé(e) est tenu(e) de reprendre son poste de travail à l'expiration exacte de la période de congé validée sur le portail numérique Somepharm.");

            document.add(body);
            document.add(Chunk.NEWLINE);
            addSignature(document);
            document.close();
        } catch (DocumentException e) { e.printStackTrace(); }
        return out.toByteArray();
    }
    // --- UTILS ---
    private void addCompanyHeader(Document document) throws DocumentException {
        Paragraph header = new Paragraph("SOMEPHARM - Ressources Humaines", headerFont);
        document.add(header);
        document.add(new Paragraph("Zone Industrielle Djasr Kasentina, Alger", textFont));
        document.add(Chunk.NEWLINE);
    }

    private void addSignature(Document document) throws DocumentException {
        String dateJour = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph signature = new Paragraph("Fait à Alger, le " + dateJour + "\n\nLa Direction des Ressources Humaines\n(Cachet et Signature)", boldFont);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
    }

    private PdfPCell createCell(String content, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(content, isHeader ? boldFont : textFont));
        cell.setPadding(8f);
        if (isHeader) cell.setBackgroundColor(new BaseColor(240, 240, 240));
        return cell;
    }
}