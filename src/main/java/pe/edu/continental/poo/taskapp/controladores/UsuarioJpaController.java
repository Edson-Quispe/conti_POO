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
import pe.edu.continental.poo.taskapp.entidades.Usuario;

/**
 *
 * @author EdsonPC
 */
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) {
        if (usuario.getInscripciones() == null) {
            usuario.setInscripciones(new ArrayList<Inscripcion>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Inscripcion> attachedInscripciones = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionesInscripcionToAttach : usuario.getInscripciones()) {
                inscripcionesInscripcionToAttach = em.getReference(inscripcionesInscripcionToAttach.getClass(), inscripcionesInscripcionToAttach.getId());
                attachedInscripciones.add(inscripcionesInscripcionToAttach);
            }
            usuario.setInscripciones(attachedInscripciones);
            em.persist(usuario);
            for (Inscripcion inscripcionesInscripcion : usuario.getInscripciones()) {
                Usuario oldUsuarioOfInscripcionesInscripcion = inscripcionesInscripcion.getUsuario();
                inscripcionesInscripcion.setUsuario(usuario);
                inscripcionesInscripcion = em.merge(inscripcionesInscripcion);
                if (oldUsuarioOfInscripcionesInscripcion != null) {
                    oldUsuarioOfInscripcionesInscripcion.getInscripciones().remove(inscripcionesInscripcion);
                    oldUsuarioOfInscripcionesInscripcion = em.merge(oldUsuarioOfInscripcionesInscripcion);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuario usuario) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getId());
            List<Inscripcion> inscripcionesOld = persistentUsuario.getInscripciones();
            List<Inscripcion> inscripcionesNew = usuario.getInscripciones();
            List<Inscripcion> attachedInscripcionesNew = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionesNewInscripcionToAttach : inscripcionesNew) {
                inscripcionesNewInscripcionToAttach = em.getReference(inscripcionesNewInscripcionToAttach.getClass(), inscripcionesNewInscripcionToAttach.getId());
                attachedInscripcionesNew.add(inscripcionesNewInscripcionToAttach);
            }
            inscripcionesNew = attachedInscripcionesNew;
            usuario.setInscripciones(inscripcionesNew);
            usuario = em.merge(usuario);
            for (Inscripcion inscripcionesOldInscripcion : inscripcionesOld) {
                if (!inscripcionesNew.contains(inscripcionesOldInscripcion)) {
                    inscripcionesOldInscripcion.setUsuario(null);
                    inscripcionesOldInscripcion = em.merge(inscripcionesOldInscripcion);
                }
            }
            for (Inscripcion inscripcionesNewInscripcion : inscripcionesNew) {
                if (!inscripcionesOld.contains(inscripcionesNewInscripcion)) {
                    Usuario oldUsuarioOfInscripcionesNewInscripcion = inscripcionesNewInscripcion.getUsuario();
                    inscripcionesNewInscripcion.setUsuario(usuario);
                    inscripcionesNewInscripcion = em.merge(inscripcionesNewInscripcion);
                    if (oldUsuarioOfInscripcionesNewInscripcion != null && !oldUsuarioOfInscripcionesNewInscripcion.equals(usuario)) {
                        oldUsuarioOfInscripcionesNewInscripcion.getInscripciones().remove(inscripcionesNewInscripcion);
                        oldUsuarioOfInscripcionesNewInscripcion = em.merge(oldUsuarioOfInscripcionesNewInscripcion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = usuario.getId();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
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
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            List<Inscripcion> inscripciones = usuario.getInscripciones();
            for (Inscripcion inscripcionesInscripcion : inscripciones) {
                inscripcionesInscripcion.setUsuario(null);
                inscripcionesInscripcion = em.merge(inscripcionesInscripcion);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
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

    public Usuario findUsuario(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    public Long login(String usuario,String clave){
        ArrayList<Usuario> usuarios = new ArrayList<>(findUsuarioEntities());
        Long resultado = -1L;
        
        for (Usuario u : usuarios) {
            if(u.getUsuario().equalsIgnoreCase(usuario)){ // El usuario existe
                if(u.getClave().equals(clave)){
                    resultado = u.getId();
                    break;
                }
            }
        }
        
        return resultado;
    }
    
    
}
