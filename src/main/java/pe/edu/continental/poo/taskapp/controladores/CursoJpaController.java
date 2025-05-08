/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.edu.continental.poo.taskapp.controladores;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import pe.edu.continental.poo.taskapp.entidades.Inscripcion;
import java.util.ArrayList;
import java.util.List;
import pe.edu.continental.poo.taskapp.controladores.exceptions.NonexistentEntityException;
import pe.edu.continental.poo.taskapp.entidades.Curso;

/**
 *
 * @author EdsonPC
 */
public class CursoJpaController implements Serializable {

    public CursoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Curso curso) {
        if (curso.getInscripciones() == null) {
            curso.setInscripciones(new ArrayList<Inscripcion>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Inscripcion> attachedInscripciones = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionesInscripcionToAttach : curso.getInscripciones()) {
                inscripcionesInscripcionToAttach = em.getReference(inscripcionesInscripcionToAttach.getClass(), inscripcionesInscripcionToAttach.getId());
                attachedInscripciones.add(inscripcionesInscripcionToAttach);
            }
            curso.setInscripciones(attachedInscripciones);
            em.persist(curso);
            for (Inscripcion inscripcionesInscripcion : curso.getInscripciones()) {
                Curso oldCursoOfInscripcionesInscripcion = inscripcionesInscripcion.getCurso();
                inscripcionesInscripcion.setCurso(curso);
                inscripcionesInscripcion = em.merge(inscripcionesInscripcion);
                if (oldCursoOfInscripcionesInscripcion != null) {
                    oldCursoOfInscripcionesInscripcion.getInscripciones().remove(inscripcionesInscripcion);
                    oldCursoOfInscripcionesInscripcion = em.merge(oldCursoOfInscripcionesInscripcion);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Curso curso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Curso persistentCurso = em.find(Curso.class, curso.getId());
            List<Inscripcion> inscripcionesOld = persistentCurso.getInscripciones();
            List<Inscripcion> inscripcionesNew = curso.getInscripciones();
            List<Inscripcion> attachedInscripcionesNew = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionesNewInscripcionToAttach : inscripcionesNew) {
                inscripcionesNewInscripcionToAttach = em.getReference(inscripcionesNewInscripcionToAttach.getClass(), inscripcionesNewInscripcionToAttach.getId());
                attachedInscripcionesNew.add(inscripcionesNewInscripcionToAttach);
            }
            inscripcionesNew = attachedInscripcionesNew;
            curso.setInscripciones(inscripcionesNew);
            curso = em.merge(curso);
            for (Inscripcion inscripcionesOldInscripcion : inscripcionesOld) {
                if (!inscripcionesNew.contains(inscripcionesOldInscripcion)) {
                    inscripcionesOldInscripcion.setCurso(null);
                    inscripcionesOldInscripcion = em.merge(inscripcionesOldInscripcion);
                }
            }
            for (Inscripcion inscripcionesNewInscripcion : inscripcionesNew) {
                if (!inscripcionesOld.contains(inscripcionesNewInscripcion)) {
                    Curso oldCursoOfInscripcionesNewInscripcion = inscripcionesNewInscripcion.getCurso();
                    inscripcionesNewInscripcion.setCurso(curso);
                    inscripcionesNewInscripcion = em.merge(inscripcionesNewInscripcion);
                    if (oldCursoOfInscripcionesNewInscripcion != null && !oldCursoOfInscripcionesNewInscripcion.equals(curso)) {
                        oldCursoOfInscripcionesNewInscripcion.getInscripciones().remove(inscripcionesNewInscripcion);
                        oldCursoOfInscripcionesNewInscripcion = em.merge(oldCursoOfInscripcionesNewInscripcion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = curso.getId();
                if (findCurso(id) == null) {
                    throw new NonexistentEntityException("The curso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Curso curso;
            try {
                curso = em.getReference(Curso.class, id);
                curso.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The curso with id " + id + " no longer exists.", enfe);
            }
            List<Inscripcion> inscripciones = curso.getInscripciones();
            for (Inscripcion inscripcionesInscripcion : inscripciones) {
                inscripcionesInscripcion.setCurso(null);
                inscripcionesInscripcion = em.merge(inscripcionesInscripcion);
            }
            em.remove(curso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Curso> findCursoEntities() {
        return findCursoEntities(true, -1, -1);
    }

    public List<Curso> findCursoEntities(int maxResults, int firstResult) {
        return findCursoEntities(false, maxResults, firstResult);
    }

    private List<Curso> findCursoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Curso.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Curso findCurso(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Curso.class, id);
        } finally {
            em.close();
        }
    }

    public int getCursoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Curso> rt = cq.from(Curso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
