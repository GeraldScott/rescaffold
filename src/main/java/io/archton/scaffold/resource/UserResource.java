package io.archton.scaffold.resource;

import io.archton.scaffold.domain.User;
import io.archton.scaffold.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "User management operations")
public class UserResource {

    private static final Logger log = Logger.getLogger(UserResource.class);

    @Inject
    UserService userService;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @APIResponse(responseCode = "200", description = "List of users retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllUsers() {
        log.debug("GET /api/users");
        try {
            List<User> users = userService.listAll();
            return Response.ok(users).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_MANAGER"})
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @APIResponse(responseCode = "200", description = "User found")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getUserById(@Parameter(description = "User ID") @PathParam("id") Long id) {
        log.debugf("GET /api/users/%s", id);
        try {
            User user = userService.findById(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(user).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Create a new user", description = "Creates a new user")
    @APIResponse(responseCode = "201", description = "User created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createUser(@Valid User user,
                               @QueryParam("password") String password,
                               @QueryParam("personId") Long personId,
                               @QueryParam("roles") List<String> roles) {
        log.debugf("POST /api/users - create with username: %s", user.username);

        try {
            User created = userService.createUser(user, password, personId, roles);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Update a user", description = "Updates an existing user")
    @APIResponse(responseCode = "200", description = "User updated successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateUser(@Parameter(description = "User ID") @PathParam("id") Long id,
                               @Valid User user,
                               @QueryParam("password") String password) {
        log.debugf("PUT /api/users/%s", id);

        try {
            User updated = userService.updateUser(id, user, password);
            return Response.ok(updated).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Delete a user", description = "Deletes an existing user")
    @APIResponse(responseCode = "204", description = "User deleted successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deleteUser(@Parameter(description = "User ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/users/%s", id);

        try {
            userService.deleteUser(id);
            return Response.noContent().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/roles/{roleName}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Add a role to a user", description = "Adds a role to an existing user")
    @APIResponse(responseCode = "200", description = "Role added successfully")
    @APIResponse(responseCode = "404", description = "User or role not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response addRoleToUser(@Parameter(description = "User ID") @PathParam("id") Long id,
                                  @Parameter(description = "Role name") @PathParam("roleName") String roleName) {
        log.debugf("POST /api/users/%s/roles/%s", id, roleName);

        try {
            User updated = userService.addRoleToUser(id, roleName);
            return Response.ok(updated).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/roles/{roleName}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Remove a role from a user", description = "Removes a role from an existing user")
    @APIResponse(responseCode = "200", description = "Role removed successfully")
    @APIResponse(responseCode = "404", description = "User or role not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response removeRoleFromUser(@Parameter(description = "User ID") @PathParam("id") Long id,
                                       @Parameter(description = "Role name") @PathParam("roleName") String roleName) {
        log.debugf("DELETE /api/users/%s/roles/%s", id, roleName);

        try {
            User updated = userService.removeRoleFromUser(id, roleName);
            return Response.ok(updated).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }
}