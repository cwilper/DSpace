/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.CollectionConverter;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.CommunityRest;
import org.dspace.app.rest.model.hateoas.CollectionResource;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.app.rest.repository.patch.DSpaceObjectPatch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(CollectionRest.CATEGORY + "." + CollectionRest.NAME)
public class CollectionRestRepository extends DSpaceObjectRestRepository<Collection, CollectionRest> {

    private final CollectionService cs;

    @Autowired
    CommunityService communityService;

    @Autowired
    public CollectionRestRepository(CollectionService dsoService,
                                    CollectionConverter dsoConverter) {
        super(dsoService, dsoConverter, new DSpaceObjectPatch<CollectionRest>() {});
        this.cs = dsoService;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'READ')")
    public CollectionRest findOne(Context context, UUID id) {
        Collection collection = null;
        try {
            collection = cs.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (collection == null) {
            return null;
        }
        return dsoConverter.fromModel(collection);
    }

    @Override
    public Page<CollectionRest> findAll(Context context, Pageable pageable) {
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        int total = 0;
        try {
            total = cs.countTotal(context);
            it = cs.findAll(context, pageable.getPageSize(), pageable.getOffset());
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = new PageImpl<Collection>(collections, pageable, total).map(dsoConverter);
        return page;
    }

    @SearchRestMethod(name = "findAuthorizedByCommunity")
    public Page<CollectionRest> findAuthorizedByCommunity(
            @Parameter(value = "uuid", required = true) UUID communityUuid, Pageable pageable) {
        Context context = obtainContext();
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        try {
            Community com = communityService.find(context, communityUuid);
            if (com == null) {
                throw new ResourceNotFoundException(
                        CommunityRest.CATEGORY + "." + CommunityRest.NAME + " with id: " + communityUuid
                        + " not found");
            }
            it = cs.findAuthorized(context, com, Constants.ADD);
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = utils.getPage(collections, pageable).map(dsoConverter);
        return page;
    }

    @SearchRestMethod(name = "findAuthorized")
    public Page<CollectionRest> findAuthorized(Pageable pageable) {
        Context context = obtainContext();
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        try {
            it = cs.findAuthorizedOptimized(context, Constants.ADD);
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = utils.getPage(collections, pageable).map(dsoConverter);
        return page;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'WRITE')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, id, patch);
    }

    @Override
    public Class<CollectionRest> getDomainClass() {
        return CollectionRest.class;
    }

    @Override
    public CollectionResource wrapResource(CollectionRest collection, String... rels) {
        return new CollectionResource(collection, utils, rels);
    }
}