using Microsoft.AspNetCore.Mvc;
using WebApiObjetos.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using WebApiObjetos.Domain;

namespace WebApiObjetos.Controllers
{

    // Modelstate.IsValid chequea que lo que le mandes en el body matchee la clase a la que lo estas bindeando.
    [Route("api/[controller]")]
    [ApiController]
    public class LocationController : Controller
    {
        private ILocationService locationsService;

        public LocationController(ILocationService locationsService)
        {
            this.locationsService = locationsService;
        }

        [HttpPost]
        [Authorize]
        public async Task<ActionResult<LocationDTO>> Post([FromBody] LocationDTO location)
        {
            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);
            location.UserId = userId;

            var result = await locationsService.AddLocation(location);

            if (result != null)
                return Created("Location Created Succesfully", result);
            else
                return Conflict("Ha ocurrido un error guardando su ubicación");
        }

        [HttpGet]
        public async Task<ActionResult<List<LocationDTO>>> Get()
        {
            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);
            var result = await locationsService.GetLocations(userId);
            return Ok(result);
        }

        [HttpGet("{locationId}")]
        [Authorize]
        public async Task<ActionResult<List<LocationDTO>>> GetLocation(int locationId)
        {
            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);
            var result = await locationsService.GetLocation(userId, locationId);
            return Ok(result);
        }

        [HttpDelete("{locationId}")]
        [Authorize]
        public async Task<ActionResult<LocationDTO>> Delete([FromRoute]int locationId)
        {

            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);

            var result = await locationsService.DeleteLocation(userId, locationId);

            if (result)
                return Ok("Location deleted succesfully");
            return Unauthorized();
        }

        [HttpPut("{locationId}")]
        [Authorize]
        public async Task<ActionResult<LocationDTO>> Update([FromRoute]int locationId, [FromBody] LocationDTO location)
        {

            location.Id = locationId;
            location.UserId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);

            var result = await locationsService.UpdateLocation(location);

            if (result)
                return Ok("Location Updated succesfully");

            return Unauthorized();
        }

        [HttpPost]
        [Route("Image")]
        [Authorize]
        public async Task<ActionResult<ImageDTO>> PostImage([FromBody] ImageDTO image)
        {

            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);
            image.UserId = userId;

            var result = await locationsService.AddImage(image);

            if (result != null)
                return Created("Image Created Succesfully", result);
            else
                return Conflict("Ha ocurrido un error guardando su imagen");
        }

        [HttpGet]
        [Route("Image/{imageId}")]
        [Authorize]
        public async Task<ActionResult<ImageDTO>> GetImage(int imageId)
        {
            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);

            var result = await locationsService.GetImage(imageId, userId);

            return Ok(result);
        }

        [HttpPost]
        [Route("LocationsInArea")]
        [Authorize]
        public async Task<ActionResult<ImageDTO>> GetLocationsInArea(LocationDTO location)
        {
            var userId = Int32.Parse(User.Claims.FirstOrDefault(x => x.Type.Equals("UserId")).Value);

            var result = await locationsService.getLocationsInArea(location.Coordinates, userId);

            return Ok(result);
        }

        [HttpGet]
        [Route("health")]
        public async Task<ActionResult<ImageDTO>> Health()
        {
            return Ok("");
        }
    }
}
