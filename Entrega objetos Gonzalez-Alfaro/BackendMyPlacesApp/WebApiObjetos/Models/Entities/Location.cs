using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Domain;

namespace WebApiObjetos.Models.Entities
{
    public class Location
    {
        public int Id { get; set; }

        public int UserId { get; set; }

        public string Tag { get; set; }

        public int Color { get; set; }

        public string Coordinates { get; set; }

        public short Type { get; set; }

        public Nullable<int> ImageId { get; set; }

        public LocationDTO toDto()
        {
            return new LocationDTO
            {
                Id = this.Id,
                UserId = this.UserId,
                Color = this.Color,
                Coordinates = this.Coordinates,
                Tag = this.Tag,
                Type = this.Type,
                ImageId = this.ImageId
            };
        }

    }
}
